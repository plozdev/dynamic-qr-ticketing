# BẢN THIẾT KẾ KỸ THUẬT VÀ LỘ TRÌNH TỰ TRIỂN KHAI (TECHNICAL DESIGN & LEARNING ROADMAP)
## Hệ Thống Dynamic QR Ticketing — Mobile (Android Kotlin + Jetpack Compose + C++ NDK)

> **Mục tiêu tài liệu:** Cung cấp toàn bộ bản thiết kế kiến trúc, ranh giới các tầng (Clean Architecture), nguyên lý bảo mật bằng C++ NDK, luồng dữ liệu một chiều (MVI Pattern), và lộ trình từng bước (Step-by-step) để bạn tự tay lập trình (code) và nắm vững kiến trúc ứng dụng Android hiện đại.

---

## 1. TỔNG QUAN KIẾN TRÚC HỆ THỐNG (SYSTEM ARCHITECTURE)

Hệ thống Dynamic QR Ticketing trên Mobile được chia làm 2 Feature chính và 3 Core Module hỗ trợ:

```
                                  +-----------------------------+
                                  |        MainActivity         |
                                  +--------------+--------------+
                                                 |
                   +-----------------------------+-----------------------------+
                   |                                                           |
     +-------------v-------------+                               +-------------v-------------+
     |   feature: ticket_display |                               |   feature: gate_scanner   |
     |   (Hiển thị Dynamic QR)   |                               |   (Quét kiểm soát tại cổng)|
     +-------------+-------------+                               +-------------+-------------+
                   |                                                           |
                   +-----------------------------+-----------------------------+
                                                 |
        +----------------------------------------+----------------------------------------+
        |                                        |                                        |
+-------v-------+                       +--------v--------+                      +--------v--------+
|   core_mvi    |                       |   core_crypto   |                      |  core_network   |
| (Base State,  |                       |  (C++ NDK, JNI, |                      |  (OkHttp client,|
| Intent, VM)   |                       |   TOTP, HMAC)   |                      |   interceptors) |
+---------------+                       +-----------------+                      +-----------------+
```

### 1.1. Clean Architecture (Feature-Based)
Mỗi feature (`ticket_display`, `gate_scanner`) tuân thủ nghiêm ngặt mô hình 3 tầng:

1. **Domain Layer (Tầng nghiệp vụ thuần túy - Pure Kotlin)**:
   - **Đặc tính**: Tuyệt đối **không** phụ thuộc Android SDK hay bất kỳ thư viện bên thứ 3 nào (ngoại trừ Kotlin Coroutines / Flow).
   - **Thành phần**:
     - `model/`: Các Domain Entities đại diện cho nghiệp vụ cốt lõi (`Ticket`, `DynamicQrData`, `GateAccessStatus`).
     - `repository/`: Các Boundary Interfaces (ví dụ `ITicketRepository`, `IGateValidationRepository`).
     - `usecase/`: Các nghiệp vụ đơn lẻ (Single-responsibility), ví dụ `GetTicketDetailUseCase`, `GenerateDynamicQrUseCase`, `ValidateScannedTicketUseCase`.
2. **Data Layer (Tầng dữ liệu)**:
   - **Đặc tính**: Triển khai các interface từ Domain Layer (Dependency Inversion Principle).
   - **Thành phần**:
     - `datasource/`: Ranh giới giao tiếp với Remote API (`ITicketRemoteDataSource`) hoặc Local Storage (`ITicketLocalDataSource`).
     - `dto/`: Data Transfer Objects nhận từ Backend hoặc Database.
     - `mapper/`: Bộ chuyển đổi thuần giữa DTO và Domain Model.
     - `repository/`: Triển khai cụ thể của Repository (`TicketRepositoryImpl`, `GateValidationRepositoryImpl`), quyết định chiến lược caching và kết nối với `core_crypto`.
3. **Presentation Layer (Tầng giao diện - Jetpack Compose & MVI)**:
   - **Đặc tính**: Phản ứng theo trạng thái duy nhất (Single Source of Truth), quản lý vòng đời (Lifecycle-aware).
   - **Thành phần**:
     - `contract/`: Chứa bộ ba định nghĩa `UiState`, `UiIntent`, `UiEffect`.
     - `ViewModel`: Kế thừa `BaseViewModel`, nhận `Intent`, gọi `UseCase`, cập nhật `State` và phát `Effect`.
     - `ui/`: Các composable UI functions thuần túy hiển thị trạng thái và bắn Intent khi có thao tác người dùng.

---

## 2. NGUYÊN LÝ BẢO MẬT & THIẾT KẾ C++ NDK (`core_crypto`)

### 2.1. Tại sao phải đưa mã hóa xuống C++ NDK?
- **Chống dịch ngược (Anti-Decompilation)**: Code Kotlin/Java khi dịch ra `.dex` rất dễ bị dịch ngược thành mã nguồn bằng Bytecode Viewer hay JADX. File thư viện C++ (`libdynamic_qr_crypto.so`) được biên dịch thành mã máy ELF nhị phân, gây khó khăn lớn cho việc dịch ngược và chỉnh sửa (tampering).
- **Native Key Obfuscation**: Các chuỗi khóa bí mật (Secret Seeds, Salts) được chia tách thành mảng byte và XOR với mặt nạ (XOR mask) lúc biên dịch, chỉ de-mask tức thời trong RAM khi tính toán.
- **Tối ưu tốc độ xoay vòng TOTP**: Việc băm liên tục HMAC/SHA-256 trong native C++ giảm tải GC (Garbage Collection) và tiết kiệm pin cho thiết bị.

### 2.2. Cơ chế Dynamic QR Token (TOTP Sliding Window)
Mã QR động thay đổi sau mỗi chu kỳ (ví dụ `interval = 30 giây`):
1. **Time Step $T$**:
   $$T = \lfloor \frac{\text{CurrentEpochMs} / 1000}{\text{IntervalSeconds}} \rfloor$$
2. **Dynamic Token Generation**:
   $$\text{Token} = \text{Truncate}(\text{HMAC-SHA256}(\text{SecretKey}, \text{TicketId} \parallel T)) \pmod{10^8}$$
3. **Sliding Time Window Verification**:
   Để tránh từ chối vé hợp lệ do độ lệch đồng hồ (Clock Skew) hoặc mạng chậm khi quét tại cổng, phía xác thực hỗ trợ cửa sổ trôi $\Delta t \in \{-1, 0, +1\}$:
   $$\text{Steps to check} = \{T - 1, T, T + 1\}$$

### 2.3. Quy ước đặt tên JNI Name Mangling
Khi Kotlin gọi C++ qua JNI, quy ước đặt tên hàm C++ là:
`Java_<package_name>_<class_name>_<method_name>`

> **Lưu ý quan trọng**: Trong chuẩn JNI, dấu gạch dưới `_` trong tên package (ví dụ `core_crypto`, `native_bridge`) được mã hóa thành `_1`.
> Ví dụ:
> Package Kotlin: `com.ticketing.mobile.core_crypto.data.native_bridge.NativeCryptoBridge`
> Tên hàm C++: `Java_com_ticketing_mobile_core_1crypto_data_native_1bridge_NativeCryptoBridge_generateDynamicTotpToken`

---

## 3. MÔ HÌNH MVI (UNIDIRECTIONAL DATA FLOW)

MVI đảm bảo trạng thái ứng dụng luôn dự đoán được (Predictable), dễ test và tránh lỗi bất đồng bộ:

```
   [User Action / Lifecycle]
              |
              v
       ( Dispatches )
              |
              v
       +-------------+
       |   UiIntent  |
       +------+------+
              |
              v
       +--------------+      Calls      +------------+
       |  ViewModel   | --------------> |  UseCases  |
       +---+------+---+                 +------------+
           |      |
 Updates   |      | Emits (One-time)
           v      v
+-------------+  +---------------+
|   UiState   |  |    UiEffect   |
| (StateFlow) |  | (Channel/Flow)|
+------+------+  +-------+-------+
       |                 |
       +--------+--------+
                |
                v
        ( Observes & Renders )
                |
                v
     +---------------------+
     | Jetpack Compose UI  |
     +---------------------+
```

### 3.1. Các thành phần của MVI Contract:
- **`UiState` (Immutable Data Class)**:
  - Đại diện cho toàn bộ trạng thái của màn hình tại một thời điểm (Loading, Data, Error).
  - Được quản lý bằng `StateFlow<State>`. Chỉ có thể thay đổi bằng cách tạo ra bản copy mới (`copy(...)`).
- **`UiIntent` (Sealed Interface)**:
  - Đại diện cho mọi ý định/hành động: Thao tác click, sự kiện timer, mã QR được camera nhận diện, chuyển đổi chế độ offline.
- **`UiEffect` (Sealed Interface)**:
  - Sự kiện một lần (Single-shot Event) không lưu lại trong state: Hiển thị Snackbar/Toast, kích hoạt rung (Haptic), phát âm thanh beep, mở màn hình mới.
  - Sử dụng `Channel<Effect>(Channel.BUFFERED)` để đảm bảo sự kiện không bị mất khi màn hình xoay ngang dọc (Configuration change).

---

## 4. LỘ TRÌNH TỰ CODE VÀ HỌC TẬP TỪNG BƯỚC (LEARNING & IMPLEMENTATION ROADMAP)

Để bạn tự tay code và hiểu sâu từng tầng, hãy thực hiện lần lượt theo 5 giai đoạn:

### Giai đoạn 1: Triển khai C++ NDK & JNI Bridge (`core_crypto`)
- [ ] **Mục tiêu**: Hiểu cách Android giao tiếp với mã C++ và cơ chế bảo vệ mã hóa.
- [ ] **Các bước thực hiện**:
  1. Mở [`crypto_engine.h`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/cpp/include/crypto_engine.h) và [`crypto_engine.cpp`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/cpp/src/crypto_engine.cpp).
  2. Viết hàm `generateTotpToken`: Tính toán Time Step `T = (timestampMs / 1000) / intervalSec`. Sử dụng thuật toán băm (ví dụ FNV-1a hoặc HMAC-SHA256) kết hợp `ticketId`, Secret Seed, và `T`.
  3. Viết hàm `verifyTotpToken`: Dùng vòng lặp duyệt từ `-allowedDriftSteps` đến `+allowedDriftSteps` để so sánh mã token.
  4. Mở [`secure_storage.cpp`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/cpp/src/secure_storage.cpp): Thiết kế mảng byte mặt nạ (XOR mask) để sinh ra Secret Key cho từng vé.
  5. Mở [`NativeCryptoBridge.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/core_crypto/data/native_bridge/NativeCryptoBridge.kt) và [`NativeCryptoEngineImpl.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/core_crypto/data/NativeCryptoEngineImpl.kt): Gọi `System.loadLibrary("dynamic_qr_crypto")` và chuyển kết quả tính toán về dạng Coroutine với `withContext(Dispatchers.Default)`.

### Giai đoạn 2: Xây dựng Network & Core MVI (`core_network` & `core_mvi`)
- [ ] **Mục tiêu**: Nắm vững xử lý bất đồng bộ Coroutine Flow và cấu trúc cơ sở cho MVI.
- [ ] **Các bước thực hiện**:
  1. Mở [`BaseViewModel.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/core_mvi/BaseViewModel.kt): Xem cách `_uiState` (`MutableStateFlow`) và `_effect` (`Channel`) được đóng gói.
  2. Mở [`OkHttpApiClient.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/core_network/client/OkHttpApiClient.kt): Viết code gửi HTTP GET / POST bằng OkHttp, thực hiện parse response và map lỗi thành [`ApiError`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/core_network/model/ApiError.kt).
  3. Mở [`AuthHeaderInterceptor.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/core_network/interceptor/AuthHeaderInterceptor.kt): Thêm header `Authorization: Bearer <token>` và `X-Device-Fingerprint`.

### Giai đoạn 3: Triển khai Feature `ticket_display`
- [ ] **Mục tiêu**: Xây dựng luồng hiển thị vé động với countdown timer tự động xoay mã.
- [ ] **Các bước thực hiện**:
  1. **Domain**:
     - Hoàn thiện [`GetTicketDetailUseCase.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/ticket_display/domain/usecase/GetTicketDetailUseCase.kt): Kiểm tra validate input `ticketId` trước khi chuyển sang Repository.
     - Hoàn thiện [`GenerateDynamicQrUseCase.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/ticket_display/domain/usecase/GenerateDynamicQrUseCase.kt).
  2. **Data**:
     - Mở [`TicketMapper.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/ticket_display/data/mapper/TicketMapper.kt): Chuyển đổi [`TicketDto`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/ticket_display/data/dto/TicketDto.kt) sang [`Ticket`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/ticket_display/domain/model/Ticket.kt).
     - Mở [`TicketRepositoryImpl.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/ticket_display/data/repository/TicketRepositoryImpl.kt):
       - Chiến lược Cache-first: Lấy từ Local Cache; nếu không có thì gọi Remote API và lưu cache.
       - Viết hàm `observeDynamicQr(ticketId)`: Tạo `flow { ... }` phát ra `DynamicQrData` mỗi giây để UI hiển thị thanh đếm ngược lùi dần. Khi hết 30s thì sinh token mới từ `ICryptoEngine`.
  3. **Presentation**:
     - Mở [`TicketDisplayViewModel.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/ticket_display/presentation/TicketDisplayViewModel.kt): Lắng nghe `TicketDisplayIntent`, cập nhật `TicketDisplayState`.
     - Mở [`TicketDisplayScreen.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/ticket_display/presentation/ui/TicketDisplayScreen.kt): Dùng Compose hiển thị card thông tin vé, ảnh mã QR và `LinearProgressIndicator` chạy theo thời gian còn lại.

### Giai đoạn 4: Triển khai Feature `gate_scanner`
- [ ] **Mục tiêu**: Xây dựng màn hình quét vé tại cổng với khả năng xác thực kép: Online API và Offline qua C++ NDK.
- [ ] **Các bước thực hiện**:
  1. **Domain**:
     - Hoàn thiện [`ValidateScannedTicketUseCase.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/gate_scanner/domain/usecase/ValidateScannedTicketUseCase.kt).
  2. **Data**:
     - Mở [`GateValidationRepositoryImpl.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/gate_scanner/data/repository/GateValidationRepositoryImpl.kt):
       - Chế độ Online: Gửi payload lên API cổng soát vé.
       - Chế độ Offline: Tách chuỗi payload `TKT:<id>|TOKEN:<token>`, chuyển thẳng vào `cryptoEngine.verifyToken(ticketId, token)` để cho phép khán giả qua cửa ngay cả khi mất mạng.
  3. **Presentation**:
     - Mở [`GateScannerViewModel.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/gate_scanner/presentation/GateScannerViewModel.kt): Xử lý intent `QrCodeScanned` (debounce tránh quét lặp 1 vé liên tục).
     - Mở [`GateScannerScreen.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/gate_scanner/presentation/ui/GateScannerScreen.kt): Vẽ khung ngắm camera, overlay đổi màu Xanh (`ACCESS GRANTED`) hoặc Đỏ (`ACCESS DENIED`).

### Giai đoạn 5: Kết nối UI Host & Kiểm thử
- [ ] **Mục tiêu**: Ghép nối các khối lại trên [`MainActivity.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/MainActivity.kt).
- [ ] **Các bước thực hiện**:
  1. Thiết lập Dependency Injection (manual DI hoặc Hilt/Koin).
  2. Tạo điều hướng giữa hai màn hình: Vé của tôi (`TicketDisplay`) và Máy quét cổng (`GateScanner`).
  3. Chạy thử nghiệm và kiểm tra Unit Test cho từng UseCase.

---

## 5. BẢNG TỔNG HỢP DANH MỤC FILE KHUNG SƯỜN (SCAFFOLD FILES)

| Phân hệ / Module | Đường dẫn file | Ý nghĩa / Nhiệm vụ ranh giới |
| :--- | :--- | :--- |
| **NDK / CMake** | [`app/src/main/cpp/CMakeLists.txt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/cpp/CMakeLists.txt) | Script build C++ thành `libdynamic_qr_crypto.so` |
| **NDK Headers** | [`app/src/main/cpp/include/crypto_engine.h`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/cpp/include/crypto_engine.h) | Khai báo các API thuật toán mã hóa C++ |
| **NDK Headers** | [`app/src/main/cpp/include/secure_storage.h`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/cpp/include/secure_storage.h) | Khai báo cơ chế che giấu khóa bí mật (Obfuscation) |
| **Core Crypto** | [`core_crypto/domain/repository/ICryptoEngine.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/core_crypto/domain/repository/ICryptoEngine.kt) | Boundary Interface gọi mã hóa |
| **Core Crypto** | [`core_crypto/data/native_bridge/NativeCryptoBridge.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/core_crypto/data/native_bridge/NativeCryptoBridge.kt) | JNI Bridge nạp thư viện `.so` |
| **Core Network** | [`core_network/client/IApiClient.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/core_network/client/IApiClient.kt) | Boundary Interface cho REST Client |
| **Core Network** | [`core_network/client/IWebSocketClient.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/core_network/client/IWebSocketClient.kt) | Boundary Interface cho Real-time Socket |
| **Core MVI** | [`core_mvi/BaseViewModel.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/core_mvi/BaseViewModel.kt) | Lớp cha điều phối StateFlow và Side Effect Channel |
| **Ticket Display** | [`ticket_display/domain/repository/ITicketRepository.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/ticket_display/domain/repository/ITicketRepository.kt) | Boundary Interface truy xuất vé & stream QR |
| **Gate Scanner** | [`gate_scanner/domain/repository/IGateValidationRepository.kt`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/src/main/java/com/ticketing/mobile/gate_scanner/domain/repository/IGateValidationRepository.kt) | Boundary Interface kiểm soát vào cổng |
| **Dependencies** | [`gradle/libs.versions.toml`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/gradle/libs.versions.toml) | Quản lý phiên bản tập trung (Version Catalog) |
| **App Build** | [`app/build.gradle.kts`](file:///D:/Projects/dynamic-qr-ticketing/.worktrees/dev-mobile/mobile/app/build.gradle.kts) | Cấu hình Android Application, Compose & NDK |
