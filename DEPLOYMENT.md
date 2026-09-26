# Triển khai bản demo CyberPass

Hướng dẫn này dùng **Supabase PostgreSQL** cho DB, **Cloud Run** cho backend, **GitHub Pages** cho frontend và APK debug trong repository. Đây là prototype: đặt vé chưa có thanh toán, DB có tài khoản demo dùng mật khẩu chung. Không dùng dữ liệu hoặc tài khoản thật. [Giới hạn của GitHub Pages](https://docs.github.com/en/pages/getting-started-with-github-pages/github-pages-limits).

## 1. Chuyển PostgreSQL Docker sang Supabase

Project Supabase đã tạo: `cyber_pass`. Backend dùng **Session pooler** (host `aws-0-ap-northeast-1.pooler.supabase.com`, port `5432`, database `postgres`, user `postgres.ewsxgjtyxejovzddbnnd`). Password **không nằm trong repository**. URL JDBC trong profile `prod` có `sslmode=require`. Session mode hỗ trợ JDBC/Flyway qua IPv4 trên gói Free. Không dùng project URL `https://…supabase.co` hoặc Transaction pooler `6543` làm JDBC URL. [Supabase: kết nối PostgreSQL](https://supabase.com/docs/guides/database/connecting-to-postgres).

Để giữ **toàn bộ** user, event, ticket, log và lịch sử Flyway từ Docker:

1. Dừng backend local để dữ liệu không đổi trong lúc dump. Mở Docker Desktop, từ `backend/` chạy:

   ```powershell
   docker compose up -d postgres
   .\migrate-to-supabase.ps1
   ```

2. Gõ `MIGRATE`, rồi nhập **database password** của Supabase khi script hỏi. Script kiểm tra `public` trên Supabase còn trống, dump schema `public` từ `ticketing-postgres`, restore schema + dữ liệu + `flyway_schema_history`, rồi kiểm tra số user/event/ticket và Flyway version. Script dừng nếu đích đã có bảng; không tự xóa dữ liệu. Backup có timestamp ở `backend/.local/`, được `.gitignore` bỏ qua. Giữ backup riêng trước khi xóa DB Docker.
3. Trong **Supabase → SQL Editor**, kiểm tra:

   ```sql
   select version, description, success
   from public.flyway_schema_history
   order by installed_rank desc limit 5;

   select (select count(*) from public.users) as users,
          (select count(*) from public.events) as events,
          (select count(*) from public.tickets) as tickets;
   ```

4. Chạy BE local với DB Supabase để Flyway xác nhận schema:

   ```powershell
   .\run-supabase.ps1
   ```

   Script hỏi password mỗi lần chạy và kích hoạt profile `prod`. Nếu chạy từ IntelliJ, đặt `SPRING_DATASOURCE_PASSWORD` trong Run Configuration; `application.yml` mặc định cũng trỏ tới Supabase. Chỉ đặt `SPRING_PROFILES_ACTIVE=local` khi muốn dùng Docker local.

Nếu muốn **DB mới chỉ có dữ liệu seed**, bỏ bước restore và chạy `run-supabase.ps1` trên `public` đang trống. Hai cách này là lựa chọn thay thế nhau. Dòng “No migrations” trên trang tổng quan Supabase nói về Supabase CLI migrations; ứng dụng này dùng bảng `public.flyway_schema_history`.

Supabase Free có giới hạn dung lượng và có thể tạm dừng project sau thời gian không hoạt động. [Supabase pricing](https://supabase.com/pricing). Trước khi mở BE công khai, đổi/xóa tài khoản demo V13 (mật khẩu `12345678`). Không đưa database password, dump hay service role key vào frontend/Git.

## 2. Triển khai BE lên Cloud Run

Tạo GCP project có billing; bật **Cloud Run**, **Cloud Build**, **Artifact Registry** và **Secret Manager**. Cloud Run có free tier nhưng vẫn có thể phát sinh chi phí vượt hạn mức. [Google Cloud Free Tier](https://docs.cloud.google.com/free/docs/free-cloud-features). Trong Secret Manager, tạo secret `cyberpass-db-password` chứa database password của Supabase. Tạo service account `cyberpass-api@PROJECT_ID.iam.gserviceaccount.com` và cấp quyền **Secret Manager Secret Accessor** trên secret. [Cloud Run và Secret Manager](https://docs.cloud.google.com/run/docs/configuring/services/secrets).

Từ `backend/`, thay `PROJECT_ID` rồi chạy. `--source .` build từ Dockerfile. [Cloud Run deploy from source](https://docs.cloud.google.com/run/docs/deploying-source-code).

```powershell
gcloud config set project PROJECT_ID
gcloud run deploy cyberpass-api `
  --source . `
  --region asia-southeast1 `
  --allow-unauthenticated `
  --memory 1Gi `
  --min-instances 0 `
  --max-instances 1 `
  --service-account cyberpass-api@PROJECT_ID.iam.gserviceaccount.com `
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod" `
  --set-secrets "SPRING_DATASOURCE_PASSWORD=cyberpass-db-password:latest"
```

Profile `prod` dùng Supabase Session Pooler mặc định; không cần Cloud SQL Private IP hoặc VPC subnet. Nếu service Cloud Run cũ đã cấu hình VPC egress, kiểm tra lại phần Networking trước khi deploy. Giữ `--max-instances 1` cho bản demo: SSE và replay cache hiện lưu trong bộ nhớ của một instance. Khi cần mở rộng nhiều instance, phải thay hai phần đó bằng cơ chế chia sẻ giữa các instance. Trạng thái check-in cuối cùng vẫn được cập nhật có điều kiện trong PostgreSQL.

Sau khi deploy, Cloud Run in ra URL HTTPS. Kiểm tra `https://CLOUD_RUN_URL/actuator/health` và `https://CLOUD_RUN_URL/api/v1/events`. Với DB đã restore, Flyway xác nhận lịch sử migration cũ. Nếu BE không khởi động, xem Cloud Run logs để tìm lỗi PostgreSQL hoặc Flyway.

Migration V13 tạo `demo01`–`demo12` với mật khẩu `12345678`. Nếu mở môi trường cho người dùng thật, cần bỏ tài khoản demo và tách seed khỏi migrations. Hệ thống chưa có thanh toán và chưa sẵn sàng cho production.

## 3. Triển khai FE lên GitHub Pages

1. Khi sẵn sàng đưa code lên GitHub, push nhánh chứa `.github/workflows/frontend-pages.yml` và `frontend/`.
2. Vào **Repository → Settings → Pages → Build and deployment**, chọn **GitHub Actions**. [Hướng dẫn Pages workflow](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages).
3. Vào **Settings → Secrets and variables → Actions → Variables**, tạo biến `VITE_API_BASE_URL` = `https://CLOUD_RUN_URL/api/v1`. Đây là URL public, **không** đặt DB password ở đây. Workflow sẽ bỏ qua deploy nếu biến này chưa được thiết lập.
4. Push thay đổi vào nhánh `dev` hoặc `main` để chạy workflow **Deploy frontend to GitHub Pages**. Nút chạy thủ công (`workflow_dispatch`) khả dụng sau khi workflow có trên nhánh mặc định `main`. FE sẽ ở `https://plozdev.github.io/dynamic-qr-ticketing/` nếu GitHub Pages của repository được bật.

Vite được cấu hình `base=/dynamic-qr-ticketing/` khi build trên Pages; local vẫn dùng `/`. Nếu đổi tên repository hoặc dùng custom domain, sửa `base` trong `frontend/vite.config.ts`. [Hướng dẫn Vite + GitHub Pages](https://vite.dev/guide/static-deploy).

## 4. Cài APK Android

Tải [CyberPass-1.0-debug.apk](mobile/releases/CyberPass-1.0-debug.apk) từ repository hoặc cài bằng:

```powershell
adb install -r mobile/releases/CyberPass-1.0-debug.apk
```

Ở màn hình đăng nhập, mở **Cấu hình địa chỉ API**, nhập `https://CLOUD_RUN_URL` rồi lưu; app tự thêm `/api/v1`. Sau đó đăng nhập user đã chuyển từ Docker sang Supabase. APK này được ký bằng **debug key** và dùng để thử nghiệm; bản phát hành chính thức cần release keystore riêng. Nếu build lại ở máy khác bằng debug key khác, Android có thể yêu cầu gỡ bản cũ trước khi cài.

## Kiểm tra luồng

1. FE đăng nhập bằng user đã có trong Docker, xem event và vé.
2. Android trỏ tới URL Cloud Run, đăng nhập cùng user và xem vé.
3. Tạo tài khoản admin theo hướng dẫn trong [README](README.md), mở check-in, quét QR trên FE. BE quyết định kết quả; mobile nhận cập nhật trạng thái qua SSE.

Redis trong `backend/docker-compose.yml` hiện phục vụ môi trường phát triển; mã BE hiện chưa dùng Redis cho auth hoặc xác thực QR. Triển khai Cloud Run + Supabase không cần Redis.
