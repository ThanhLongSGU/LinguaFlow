# TỪ ĐIỂN DỮ LIỆU (DATA DICTIONARY) – LINGUAFLOW

**Phiên bản:** DataDictionary(2)  
**Căn cứ:** ERD, SRS, BFD và các quyết định nghiệp vụ đã chốt.

## Quy ước

- **PK:** Khóa chính (Primary Key).
- **FK:** Khóa ngoại (Foreign Key).
- **UK:** Ràng buộc duy nhất (Unique Key).
- **NULL:** Cho phép không có giá trị.
- **NOT NULL:** Bắt buộc có giá trị.
- Các kiểu dữ liệu dưới đây là định hướng triển khai trên SQL Server, cần được giữ nhất quán giữa migration Flyway, Entity và API DTO.

---

## 1. Nhóm quản trị, danh mục và ngôn ngữ

### 1.1. Users – Tài khoản quản trị

Lưu duy nhất một tài khoản Super Admin `admin`, được seed tự động khi hệ thống khởi chạy lần đầu. Không cung cấp chức năng đăng ký hoặc tạo thêm tài khoản.

| Thuộc tính   | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                         |
| ------------ | -------------------- | ------------ | ----------------------------- |
| UserId       | INT                  | PK, NOT NULL | Mã tài khoản quản trị         |
| Username     | NVARCHAR(50)         | UK, NOT NULL | Tên đăng nhập cố định `admin` |
| PasswordHash | NVARCHAR(255)        | NOT NULL     | Mật khẩu đã băm bằng BCrypt   |
| FullName     | NVARCHAR(100)        | NOT NULL     | Tên hiển thị của Super Admin  |
| IsActive     | BIT                  | NOT NULL     | Trạng thái tài khoản          |
| CreatedAt    | DATETIME2            | NOT NULL     | Thời điểm tạo                 |
| UpdatedAt    | DATETIME2            | NOT NULL     | Thời điểm cập nhật gần nhất   |

### 1.2. Categories – Danh mục địa điểm

Phân loại POI theo tính chất hoặc loại hình địa điểm. Không sử dụng bảng này để đại diện cho phân khu địa lý.

| Thuộc tính  | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả          |
| ----------- | -------------------- | ------------ | -------------- |
| CategoryId  | INT                  | PK, NOT NULL | Mã danh mục    |
| Name        | NVARCHAR(100)        | NOT NULL     | Tên danh mục   |
| Description | NVARCHAR(MAX)        | NULL         | Mô tả danh mục |

### 1.3. Regions – Phân khu địa lý

Lưu các phân khu dùng để nhóm POI và xác định phạm vi tải trước Audio. Đây là thực thể riêng, độc lập với `Categories`.

| Thuộc tính  | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                |
| ----------- | -------------------- | ------------ | -------------------- |
| RegionId    | INT                  | PK, NOT NULL | Mã phân khu          |
| Name        | NVARCHAR(150)        | NOT NULL     | Tên phân khu         |
| Description | NVARCHAR(MAX)        | NULL         | Mô tả phân khu       |
| IsActive    | BIT                  | NOT NULL     | Trạng thái hoạt động |
| CreatedAt   | DATETIME2            | NOT NULL     | Thời điểm tạo        |
| UpdatedAt   | DATETIME2            | NOT NULL     | Thời điểm cập nhật   |

### 1.4. Languages – Ngôn ngữ

Quản lý danh sách ngôn ngữ nội dung thuyết minh và Chat AI.

| Thuộc tính | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                                                                       |
| ---------- | -------------------- | ------------ | --------------------------------------------------------------------------- |
| LanguageId | INT                  | PK, NOT NULL | Mã ngôn ngữ                                                                 |
| Code       | VARCHAR(10)          | UK, NOT NULL | Mã locale, ví dụ `vi-VN`, `en-US`, `zh-CN`, `ja-JP`, `hi-IN`, `fr-FR`, `ar` |
| Name       | NVARCHAR(50)         | NOT NULL     | Tên ngôn ngữ                                                                |
| IsActive   | BIT                  | NOT NULL     | Ngôn ngữ còn được hệ thống hỗ trợ hay không                                 |

**Bảy ngôn ngữ khởi tạo:** `vi-VN`, `en-US`, `zh-CN`, `ja-JP`, `hi-IN`, `fr-FR`, `ar`.

---

## 2. Nhóm POI, phân khu, QR và đồng bộ dữ liệu

### 2.1. TouristSites – Địa điểm tham quan

Lưu thông tin POI, tọa độ GPS, cấu hình Geofencing và liên kết phân loại/phân khu.

| Thuộc tính           | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                                                    |
| -------------------- | -------------------- | ------------ | -------------------------------------------------------- |
| SiteId               | INT                  | PK, NOT NULL | Mã định danh POI                                         |
| CategoryId           | INT                  | FK, NOT NULL | Tham chiếu `Categories.CategoryId`                       |
| RegionId             | INT                  | FK, NOT NULL | Tham chiếu `Regions.RegionId`; xác định phân khu của POI |
| CreatedBy            | INT                  | FK, NOT NULL | Tham chiếu `Users.UserId`, tài khoản tạo POI             |
| Name                 | NVARCHAR(200)        | NOT NULL     | Tên địa điểm                                             |
| Description          | NVARCHAR(MAX)        | NULL         | Mô tả địa điểm                                           |
| Address              | NVARCHAR(255)        | NULL         | Địa chỉ                                                  |
| Latitude             | DECIMAL(10,7)        | NOT NULL     | Vĩ độ WGS84                                              |
| Longitude            | DECIMAL(10,7)        | NOT NULL     | Kinh độ WGS84                                            |
| TriggerRadius        | INT                  | NOT NULL     | Bán kính Geofence, đơn vị mét                            |
| ImageUrl             | NVARCHAR(500)        | NULL         | Đường dẫn ảnh POI                                        |
| ReferenceTicketPrice | DECIMAL(12,2)        | NULL         | Giá vé tham khảo                                         |
| Status               | BIT                  | NOT NULL     | Trạng thái hoạt động của POI                             |
| UpdatedAt            | DATETIME2            | NOT NULL     | Thời điểm cập nhật gần nhất                              |

**Quy tắc:** Mỗi POI thuộc một phân khu thông qua `RegionId`. GPS có thể xác định phân khu từ dữ liệu POI; QR xác định POI trước rồi suy ra phân khu tương ứng. Ảnh tải lên không vượt quá 5MB.

### 2.2. SiteLanguages – Cấu hình ngôn ngữ theo POI

Xác định ngôn ngữ nào được bật hoặc tắt đối với từng POI. Không suy ra trạng thái bật/tắt chỉ từ việc bản dịch đã tồn tại hay chưa.

| Thuộc tính     | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                                  |
| -------------- | -------------------- | ------------ | -------------------------------------- |
| SiteLanguageId | INT                  | PK, NOT NULL | Mã cấu hình ngôn ngữ theo POI          |
| SiteId         | INT                  | FK, NOT NULL | Tham chiếu `TouristSites.SiteId`       |
| LanguageId     | INT                  | FK, NOT NULL | Tham chiếu `Languages.LanguageId`      |
| IsEnabled      | BIT                  | NOT NULL     | Ngôn ngữ có được bật cho POI hay không |
| UpdatedAt      | DATETIME2            | NOT NULL     | Thời điểm cập nhật                     |

**Ràng buộc:** Tạo unique constraint kép `(SiteId, LanguageId)` để mỗi POI chỉ có một cấu hình cho một ngôn ngữ.

### 2.3. PoiSyncMetadata – Phiên bản đồng bộ POI

Lưu phiên bản đồng bộ dữ liệu POI dùng chung cho các thiết bị du khách.

| Thuộc tính  | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                               |
| ----------- | -------------------- | ------------ | ----------------------------------- |
| MetadataId  | INT                  | PK, NOT NULL | Khóa singleton, luôn có giá trị `1` |
| DataVersion | BIGINT               | NOT NULL     | Phiên bản đồng bộ toàn cục          |
| UpdatedAt   | DATETIME2            | NOT NULL     | Thời điểm cập nhật phiên bản        |

**Quy tắc:** `DataVersion` chỉ tăng khi dữ liệu thuộc gói đồng bộ POI thay đổi theo phạm vi đã thống nhất. Việc TTS tạo xong, Audio đổi trạng thái hoặc tạo lại tệp Audio không tự động làm tăng `DataVersion` nếu không làm thay đổi dữ liệu trong gói đồng bộ.

### 2.4. QRCodes – Mã QR của POI

Lưu mã QR dùng để nhận diện POI, bao gồm trường hợp thiết bị không có quyền hoặc mất tín hiệu GPS.

| Thuộc tính  | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                            |
| ----------- | -------------------- | ------------ | -------------------------------- |
| QrId        | INT                  | PK, NOT NULL | Mã bản ghi QR                    |
| SiteId      | INT                  | FK, NOT NULL | Tham chiếu `TouristSites.SiteId` |
| QrCodeValue | NVARCHAR(500)        | UK, NOT NULL | Giá trị định danh QR             |
| IsActive    | BIT                  | NOT NULL     | QR có còn hiệu lực hay không     |
| CreatedAt   | DATETIME2            | NOT NULL     | Thời điểm tạo                    |

### 2.5. QRScanLogs – Lịch sử quét QR

Ghi nhận sự kiện quét QR phục vụ thống kê lượt quét. Không dùng bảng này để lưu lịch sử vị trí của du khách.

| Thuộc tính | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                                  |
| ---------- | -------------------- | ------------ | -------------------------------------- |
| ScanId     | BIGINT               | PK, NOT NULL | Mã sự kiện quét                        |
| QrId       | INT                  | FK, NOT NULL | Tham chiếu `QRCodes.QrId`              |
| SessionId  | UNIQUEIDENTIFIER     | FK, NOT NULL | Tham chiếu `VisitorSessions.SessionId` |
| ScannedAt  | DATETIME2            | NOT NULL     | Thời điểm quét                         |

---

## 3. Nhóm nội dung thuyết minh, bản dịch và Audio

### 3.1. Narrations – Bài thuyết minh

Đại diện cho bài thuyết minh gắn với một POI. Mỗi POI có đúng một bài thuyết minh; nội dung theo từng ngôn ngữ được lưu trong `Translations`, không lưu riêng bản thảo tiếng Việt tại bảng này.

| Thuộc tính  | Kiểu dữ liệu đề xuất | Ràng buộc        | Mô tả                                                                    |
| ----------- | -------------------- | ---------------- | ------------------------------------------------------------------------ |
| NarrationId | INT                  | PK, NOT NULL     | Mã bài thuyết minh                                                       |
| SiteId      | INT                  | FK, UK, NOT NULL | Tham chiếu `TouristSites.SiteId`; UNIQUE bảo đảm mỗi POI có đúng một bài |
| CreatedAt   | DATETIME2            | NOT NULL         | Thời điểm tạo                                                            |
| UpdatedAt   | DATETIME2            | NOT NULL         | Thời điểm cập nhật                                                       |

### 3.2. Translations – Nội dung theo ngôn ngữ

Lưu nội dung thuyết minh của từng ngôn ngữ. Mỗi bài thuyết minh chỉ có tối đa một bản ghi cho mỗi ngôn ngữ.

| Thuộc tính     | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                                                               |
| -------------- | -------------------- | ------------ | ------------------------------------------------------------------- |
| TranslationId  | INT                  | PK, NOT NULL | Mã bản dịch                                                         |
| NarrationId    | INT                  | FK, NOT NULL | Tham chiếu `Narrations.NarrationId`                                 |
| LanguageId     | INT                  | FK, NOT NULL | Tham chiếu `Languages.LanguageId`                                   |
| Content        | NVARCHAR(MAX)        | NOT NULL     | Nội dung thuyết minh của ngôn ngữ tương ứng                         |
| SourceType     | VARCHAR(20)          | NOT NULL     | Nguồn nội dung: `MANUAL`, `AI_ADMIN`, `AI_ON_DEMAND`                |
| ReviewStatus   | VARCHAR(20)          | NOT NULL     | Trạng thái duyệt: `NOT_REQUIRED`, `PENDING`, `APPROVED`, `REJECTED` |
| ContentStatus  | VARCHAR(20)          | NOT NULL     | Trạng thái nội dung: `AVAILABLE`, `STALE`                           |
| ContentHash    | VARCHAR(64)          | NOT NULL     | Hash nội dung để phát hiện thay đổi                                 |
| ContentVersion | INT                  | NOT NULL     | Phiên bản nội dung                                                  |
| ReviewedBy     | INT                  | FK, NULL     | Tham chiếu `Users.UserId`, Admin đã duyệt                           |
| ReviewedAt     | DATETIME2            | NULL         | Thời điểm duyệt                                                     |
| CreatedAt      | DATETIME2            | NOT NULL     | Thời điểm tạo                                                       |
| UpdatedAt      | DATETIME2            | NOT NULL     | Thời điểm cập nhật                                                  |

**Quy tắc nghiệp vụ:**

- Bản `vi-VN` và `en-US` do Super Admin biên soạn thủ công, có `SourceType = MANUAL`.
- `en-US` là ngôn ngữ nguồn để AI dịch sang các ngôn ngữ bổ sung.
- Bản dịch AI trong quy trình quản trị có `SourceType = AI_ADMIN`, ban đầu chờ Admin kiểm tra và duyệt (`PENDING`).
- Bản dịch được tạo theo yêu cầu của du khách có `SourceType = AI_ON_DEMAND`; nếu tạo thành công thì được phép trả về ngay theo SRS, không phải chờ Admin duyệt.
- `ReviewStatus` mô tả quy trình duyệt; `ContentStatus` mô tả nội dung còn khớp với nguồn/phiên bản hiện tại hay không. Hai trạng thái không thay thế nhau.
- Unique constraint kép `(NarrationId, LanguageId)`.

### 3.3. AiTranslationTasks – Tác vụ dịch AI

Quản lý hàng đợi dịch thuật bất đồng bộ. Phân biệt tác vụ do Admin khởi tạo và tác vụ phát sinh từ yêu cầu của du khách.

| Thuộc tính        | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                                                      |
| ----------------- | -------------------- | ------------ | ---------------------------------------------------------- |
| TaskId            | BIGINT               | PK, NOT NULL | Mã tác vụ dịch                                             |
| NarrationId       | INT                  | FK, NOT NULL | Tham chiếu `Narrations.NarrationId`                        |
| LanguageId        | INT                  | FK, NOT NULL | Tham chiếu `Languages.LanguageId`, ngôn ngữ đích           |
| TaskSource        | VARCHAR(20)          | NOT NULL     | Nguồn tác vụ: `ADMIN`, `VISITOR_REQUEST`                   |
| Status            | VARCHAR(20)          | NOT NULL     | Trạng thái: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED` |
| SourceContentHash | VARCHAR(64)          | NOT NULL     | Hash nội dung nguồn `en-US` tại thời điểm tạo tác vụ       |
| ErrorMessage      | NVARCHAR(MAX)        | NULL         | Thông tin lỗi nếu xử lý thất bại                           |
| RetryCount        | INT                  | NOT NULL     | Số lần thử lại                                             |
| CreatedAt         | DATETIME2            | NOT NULL     | Thời điểm tạo tác vụ                                       |
| UpdatedAt         | DATETIME2            | NOT NULL     | Thời điểm cập nhật trạng thái                              |

**Quy tắc:** Tác vụ `VISITOR_REQUEST` phục vụ dịch theo yêu cầu du khách và không được buộc đi qua bước duyệt quản trị. Tác vụ `ADMIN` thuộc quy trình tạo bản dịch để Admin kiểm tra.

### 3.4. Audio – Tệp âm thanh thuyết minh

Lưu thông tin quản lý tệp Audio do TTS tạo ra. Dữ liệu nhị phân của tệp được lưu trên máy chủ hoặc dịch vụ lưu trữ tệp bền vững; SQL Server chỉ lưu đường dẫn và metadata.

| Thuộc tính    | Kiểu dữ liệu đề xuất | Ràng buộc        | Mô tả                                                                               |
| ------------- | -------------------- | ---------------- | ----------------------------------------------------------------------------------- |
| AudioId       | BIGINT               | PK, NOT NULL     | Mã tệp Audio                                                                        |
| TranslationId | INT                  | FK, UK, NOT NULL | Tham chiếu `Translations.TranslationId`; tối đa một Audio hiện tại cho mỗi bản dịch |
| StorageUrl    | NVARCHAR(1000)       | NULL             | Đường dẫn tệp Audio trên hệ thống lưu trữ                                           |
| Status        | VARCHAR(20)          | NOT NULL         | Trạng thái: `PENDING`, `PROCESSING`, `READY`, `FAILED`, `STALE`                     |
| ContentHash   | VARCHAR(64)          | NOT NULL         | Hash nội dung dùng để tạo Audio                                                     |
| AudioVersion  | INT                  | NOT NULL         | Phiên bản Audio                                                                     |
| ErrorMessage  | NVARCHAR(MAX)        | NULL             | Thông tin lỗi TTS nếu có                                                            |
| RetryCount    | INT                  | NOT NULL         | Số lần thử lại                                                                      |
| GeneratedAt   | DATETIME2            | NULL             | Thời điểm tạo Audio thành công                                                      |
| UpdatedAt     | DATETIME2            | NOT NULL         | Thời điểm cập nhật trạng thái                                                       |

**Quy tắc nghiệp vụ:**

- Audio được tạo bằng TTS trên máy chủ thông qua tác vụ xử lý nền.
- Trạng thái Audio độc lập với trạng thái dịch thuật và trạng thái duyệt bản dịch.
- Chỉ Audio có `Status = READY` mới được cung cấp cho Client tải trước theo phân khu.
- Khi nội dung bản dịch thay đổi, Audio có hash không còn khớp phải được đánh dấu `STALE` hoặc đưa vào quy trình tạo lại. Client tuyệt đối không phát Audio stale.
- Client dùng `AudioVersion` hoặc định danh phiên bản tương đương để chỉ tải tệp còn thiếu hoặc đã thay đổi.
- Nếu Audio ngôn ngữ đang chọn chưa sẵn sàng, logic API có thể áp dụng Audio `en-US` làm phương án dự phòng nếu có sẵn. Không tạo bản ghi giả khiến Audio `en-US` bị hiểu nhầm là Audio của ngôn ngữ đang chọn.

---

## 4. Nhóm Chat AI và cấu hình hệ thống

### 4.1. AiConfigurations – Cấu hình AI

Lưu cấu hình hành vi Chatbot và thông tin đồng bộ tri thức.

| Thuộc tính     | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                                                              |
| -------------- | -------------------- | ------------ | ------------------------------------------------------------------ |
| ConfigId       | INT                  | PK, NOT NULL | Mã cấu hình                                                        |
| SystemPrompt   | NVARCHAR(MAX)        | NOT NULL     | Prompt định hình vai trò và cách phản hồi của trợ lý               |
| BannedKeywords | NVARCHAR(MAX)        | NULL         | Danh sách từ khóa cấm                                              |
| LastSyncedAt   | DATETIME2            | NULL         | Thời điểm đồng bộ dữ liệu POI/nội dung đã duyệt vào Knowledge Base |
| UpdatedBy      | INT                  | FK, NOT NULL | Tham chiếu `Users.UserId`                                          |

### 4.2. ChatUsageStats – Thống kê sử dụng Chat AI

Chỉ lưu số liệu tổng hợp, không lưu toàn bộ nội dung câu hỏi và câu trả lời của từng cuộc hội thoại.

| Thuộc tính      | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                             |
| --------------- | -------------------- | ------------ | --------------------------------- |
| ChatUsageStatId | BIGINT               | PK, NOT NULL | Mã bản ghi thống kê               |
| StatDate        | DATE                 | NOT NULL     | Ngày thống kê                     |
| LanguageId      | INT                  | FK, NOT NULL | Tham chiếu `Languages.LanguageId` |
| RequestCount    | INT                  | NOT NULL     | Tổng số yêu cầu Chat AI           |
| BlockedCount    | INT                  | NOT NULL     | Số yêu cầu bị chặn/từ chối        |
| FailedCount     | INT                  | NOT NULL     | Số yêu cầu xử lý thất bại         |
| UpdatedAt       | DATETIME2            | NOT NULL     | Thời điểm cập nhật số liệu        |

**Ràng buộc:** Unique constraint kép `(StatDate, LanguageId)` để mỗi ngày và ngôn ngữ có một bản ghi tổng hợp.

### 4.3. ChatFeedback – Phản hồi tự nguyện về Chat AI

Lưu phản hồi hoặc đoạn hội thoại do du khách chủ động chọn gửi sau khi xem lại và xác nhận. Không dùng bảng này để tự động ghi toàn bộ lịch sử chat.

| Thuộc tính       | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                                          |
| ---------------- | -------------------- | ------------ | ---------------------------------------------- |
| FeedbackId       | BIGINT               | PK, NOT NULL | Mã phản hồi                                    |
| LanguageId       | INT                  | FK, NOT NULL | Tham chiếu `Languages.LanguageId`              |
| SubmittedExcerpt | NVARCHAR(MAX)        | NULL         | Đoạn hội thoại được du khách chủ động chọn gửi |
| FeedbackText     | NVARCHAR(MAX)        | NULL         | Nội dung góp ý/báo cáo                         |
| FeedbackType     | VARCHAR(30)          | NOT NULL     | Loại phản hồi                                  |
| CreatedAt        | DATETIME2            | NOT NULL     | Thời điểm gửi                                  |

**Quy tắc:** Client phải cho du khách xem lại nội dung dự kiến gửi và yêu cầu xác nhận trước khi gửi. Chỉ lưu phần nội dung được chủ động gửi.

---

## 5. Nhóm phiên truy cập và thống kê hoạt động

### 5.1. VisitorSessions – Phiên truy cập ẩn danh

Ghi nhận phiên sử dụng website để tính Online Visitors và Active GPS Sessions. Không yêu cầu tài khoản du khách và không lưu danh tính thực.

| Thuộc tính         | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                                                            |
| ------------------ | -------------------- | ------------ | ---------------------------------------------------------------- |
| SessionId          | UNIQUEIDENTIFIER     | PK, NOT NULL | Mã phiên truy cập ngẫu nhiên                                     |
| StartedAt          | DATETIME2            | NOT NULL     | Thời điểm bắt đầu phiên                                          |
| LastHeartbeatAt    | DATETIME2            | NOT NULL     | Thời điểm heartbeat website gần nhất                             |
| LastGpsHeartbeatAt | DATETIME2            | NULL         | Thời điểm cập nhật GPS gần nhất; NULL nếu không có heartbeat GPS |
| EndedAt            | DATETIME2            | NULL         | Thời điểm kết thúc phiên nếu có                                  |

**Quy tắc nghiệp vụ:**

- `LastHeartbeatAt` dùng để xác định phiên truy cập website còn hoạt động.
- `LastGpsHeartbeatAt` chỉ được cập nhật khi du khách đã cấp quyền GPS và Client thực sự gửi cập nhật vị trí.
- Một phiên website đang hoạt động không đồng nghĩa với một phiên GPS đang hoạt động.
- Ngưỡng thời gian xác định phiên còn hoạt động được cấu hình thống nhất phía Server, không cố định trong cấu trúc bảng.
- Không lưu tọa độ GPS hoặc lịch sử di chuyển trong bảng này.

### 5.2. AudioListenLogs – Lịch sử lượt nghe Audio

Ghi nhận sự kiện phát Audio phục vụ thống kê lượt nghe và POI được quan tâm.

| Thuộc tính | Kiểu dữ liệu đề xuất | Ràng buộc    | Mô tả                                  |
| ---------- | -------------------- | ------------ | -------------------------------------- |
| ListenId   | BIGINT               | PK, NOT NULL | Mã sự kiện nghe                        |
| AudioId    | BIGINT               | FK, NOT NULL | Tham chiếu `Audio.AudioId`             |
| SiteId     | INT                  | FK, NOT NULL | Tham chiếu `TouristSites.SiteId`       |
| SessionId  | UNIQUEIDENTIFIER     | FK, NOT NULL | Tham chiếu `VisitorSessions.SessionId` |
| PlayedAt   | DATETIME2            | NOT NULL     | Thời điểm bắt đầu phát                 |

---

## 6. Quan hệ dữ liệu và ràng buộc quan trọng

1. `Categories` 1–N `TouristSites`: một danh mục có thể phân loại nhiều POI.
2. `Regions` 1–N `TouristSites`: một phân khu chứa nhiều POI; mỗi POI thuộc một phân khu.
3. `Users` 1–N `TouristSites`: tài khoản Super Admin tạo/quản lý POI.
4. `TouristSites` 1–N `SiteLanguages`; `Languages` 1–N `SiteLanguages`.
5. `TouristSites` 1–N `QRCodes`; `QRCodes` 1–N `QRScanLogs`.
6. `VisitorSessions` 1–N `QRScanLogs` và `AudioListenLogs`.
7. `TouristSites` 1–1 `Narrations`; bắt buộc unique constraint trên `Narrations.SiteId`.
8. `Narrations` 1–N `Translations`; bắt buộc unique constraint kép `(NarrationId, LanguageId)`.
9. `Languages` 1–N `Translations` và `AiTranslationTasks`.
10. `Narrations` 1–N `AiTranslationTasks`.
11. `Translations` 1–0..1 `Audio`; bắt buộc unique constraint trên `Audio.TranslationId`.
12. `Audio` 1–N `AudioListenLogs`.
13. `Languages` 1–N `ChatUsageStats` và `ChatFeedback`.
14. `Users` 1–N `AiConfigurations`; `Users` cũng được tham chiếu từ `Translations.ReviewedBy`.

## 7. Những điểm cần lưu ý khi triển khai

- Không tạo `ChatAuditLogs` để lưu mặc định toàn bộ `UserQuestion` và `BotResponse`; điều này trái với quyết định lưu lịch sử hội thoại cá nhân trên thiết bị.
- Không đặt `AudioUrl` trong `Translations`; thông tin tệp và trạng thái TTS thuộc bảng `Audio`.
- Không đặt `OriginalContent` tiếng Việt trong `Narrations`; tiếng Việt và tiếng Anh Mỹ đều là các bản ghi `Translations` được biên soạn thủ công.
- Không dùng `Categories` thay cho `Regions`.
- Không dùng sự tồn tại của `Translations` để suy ra `SiteLanguages.IsEnabled`.
- Không đồng nhất `ReviewStatus`, `ContentStatus` và `Audio.Status`.
- Không tăng `DataVersion` chỉ vì tác vụ TTS hoàn thành hoặc trạng thái Audio thay đổi.
- Không lưu dữ liệu nhị phân Audio trong SQL Server.
- Khi triển khai, cần dùng migration để tạo FK, UK, NOT NULL, CHECK constraint cho các giá trị trạng thái và index phục vụ truy vấn; không chỉ dựa vào mô tả trong tài liệu.
