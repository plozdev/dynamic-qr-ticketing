# Triển khai bản demo CyberPass

Hướng dẫn này dùng **Cloud Run** cho backend, **Cloud SQL PostgreSQL** cho DB, **GitHub Pages** cho frontend và một APK debug tải từ repository. Đây là môi trường demo: luồng đặt vé chưa có thanh toán, migration V13 tạo user có mật khẩu chung và GitHub Pages không phù hợp cho giao dịch chứa dữ liệu nhạy cảm. Không dùng tài khoản hoặc dữ liệu thật. [Giới hạn của GitHub Pages](https://docs.github.com/en/pages/getting-started-with-github-pages/github-pages-limits).

## 1. Tạo DB trên Google Cloud

1. Tạo GCP project có billing, chọn cùng một region cho Cloud SQL và Cloud Run (ví dụ `asia-southeast1`). Bật Cloud Run, Cloud Build, Artifact Registry, Cloud SQL Admin, Secret Manager, Compute Engine và Service Networking API.
2. Trong **Cloud SQL → Create instance**, chọn PostgreSQL 16, instance nhỏ phù hợp ngân sách, **Private IP** trên VPC `default`. Nếu được yêu cầu, tạo **Private Service Access** cho VPC. Ghi lại **private IP** của instance. [Hướng dẫn Private IP](https://docs.cloud.google.com/sql/docs/postgres/configure-private-ip).
3. Tạo database `ticketing_db` và DB user `ticketing_user` với mật khẩu mạnh. Tạo secret `cyberpass-db-password` trong Secret Manager chứa mật khẩu này. Tạo service account cho Cloud Run và cấp quyền **Secret Manager Secret Accessor** đối với secret. [Cloud Run và Secret Manager](https://docs.cloud.google.com/run/docs/configuring/services/secrets).

Cloud SQL tính phí instance trong thời gian chạy, kể cả khi không có request; kiểm tra [bảng giá Cloud SQL](https://cloud.google.com/sql/pricing) trước khi tạo.

## 2. Triển khai BE lên Cloud Run

BE đã có `backend/Dockerfile` và profile `prod`. Từ thư mục `backend/`, chạy lệnh sau sau khi thay `PROJECT_ID` và `PRIVATE_IP`. `--source .` sẽ build từ Dockerfile. [Tài liệu Cloud Run deploy from source](https://docs.cloud.google.com/run/docs/deploying-source-code).

```powershell
gcloud config set project PROJECT_ID
gcloud run deploy cyberpass-api `
  --source . `
  --region asia-southeast1 `
  --allow-unauthenticated `
  --network default `
  --subnet default `
  --vpc-egress private-ranges-only `
  --memory 1Gi `
  --max-instances 2 `
  --service-account cyberpass-api@PROJECT_ID.iam.gserviceaccount.com `
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod,SPRING_DATASOURCE_URL=jdbc:postgresql://PRIVATE_IP:5432/ticketing_db?sslmode=require,SPRING_DATASOURCE_USERNAME=ticketing_user" `
  --set-secrets "SPRING_DATASOURCE_PASSWORD=cyberpass-db-password:1"
```

Tạo service account `cyberpass-api@PROJECT_ID.iam.gserviceaccount.com` trước khi chạy lệnh. Cloud Run phải dùng **Direct VPC egress** trên cùng VPC với Cloud SQL; với private IP, JDBC kết nối trực tiếp tới `PRIVATE_IP:5432`. [Cloud Run → Cloud SQL private IP](https://docs.cloud.google.com/sql/docs/postgres/connect-run), [Direct VPC egress](https://docs.cloud.google.com/run/docs/configuring/vpc-direct-vpc).

Sau khi deploy, Cloud Run in ra URL HTTPS. Kiểm tra `https://CLOUD_RUN_URL/actuator/health` và `https://CLOUD_RUN_URL/api/v1/events`. Flyway chạy khi BE khởi động, tạo schema và dữ liệu demo. DB trên GCP **khác** DB Docker local; chỉ có dữ liệu từ migrations, không tự chuyển các vé/user bạn đã tạo thủ công ở local. Có thể xem và chạy SQL trong [Cloud SQL Studio](https://docs.cloud.google.com/sql/docs/postgres/manage-data-using-studio).

Migration V13 tạo `demo01`–`demo12` với mật khẩu `12345678`. Nếu muốn mở môi trường cho người dùng thật, cần tách seed demo khỏi migrations và loại bỏ các tài khoản demo trước khi cấp quyền truy cập công khai. Hệ thống hiện chưa có thanh toán và chưa sẵn sàng cho vận hành production.

## 3. Triển khai FE lên GitHub Pages

1. Push nhánh chứa `.github/workflows/frontend-pages.yml` và `frontend/` lên GitHub.
2. Vào **Repository → Settings → Pages → Build and deployment**, chọn **GitHub Actions**. [Hướng dẫn Pages workflow](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages).
3. Vào **Settings → Secrets and variables → Actions → Variables**, tạo biến `VITE_API_BASE_URL` = `https://CLOUD_RUN_URL/api/v1`. Đây là URL public, **không** đặt DB password ở đây. Workflow sẽ bỏ qua deploy nếu biến này chưa được thiết lập.
4. Push thay đổi vào nhánh `dev` hoặc `main` để chạy workflow **Deploy frontend to GitHub Pages**. Nút chạy thủ công (`workflow_dispatch`) khả dụng sau khi workflow có trên nhánh mặc định `main`. FE sẽ ở `https://plozdev.github.io/dynamic-qr-ticketing/` nếu GitHub Pages của repository được bật.

Vite được cấu hình `base=/dynamic-qr-ticketing/` khi build trên Pages; local vẫn dùng `/`. Nếu đổi tên repository hoặc dùng custom domain, sửa `base` trong `frontend/vite.config.ts`. [Hướng dẫn Vite + GitHub Pages](https://vite.dev/guide/static-deploy).

## 4. Cài APK Android

Tải [CyberPass-1.0-debug.apk](mobile/releases/CyberPass-1.0-debug.apk) từ repository hoặc cài bằng:

```powershell
adb install -r mobile/releases/CyberPass-1.0-debug.apk
```

Ở màn hình đăng nhập, mở **Cấu hình địa chỉ API**, nhập `https://CLOUD_RUN_URL` rồi lưu; app tự thêm `/api/v1`. Sau đó đăng nhập tài khoản demo. APK này được ký bằng **debug key** và dùng để thử nghiệm; bản phát hành chính thức cần release keystore riêng. Nếu build lại ở máy khác bằng debug key khác, Android có thể yêu cầu gỡ bản cũ trước khi cài.

## Kiểm tra luồng

1. FE đăng nhập `demo01` / `12345678`, xem event và đặt vé.
2. Android trỏ tới URL Cloud Run, đăng nhập cùng user và xem vé.
3. Tạo tài khoản admin theo hướng dẫn trong [README](README.md), mở check-in, quét QR trên FE. BE quyết định kết quả; mobile nhận cập nhật trạng thái qua SSE.

Redis trong `backend/docker-compose.yml` hiện phục vụ môi trường phát triển; mã BE hiện chưa dùng Redis cho auth hoặc xác thực QR. Triển khai Cloud Run + Cloud SQL theo tài liệu này chưa cần Memorystore.
