TỪ ĐIỂN DỮ LIỆU (DATA DICTIONARY) - LINGUAFLOW
Quy ước: PK (Khóa chính), FK (Khóa ngoại), UK (Khóa duy nhất).

1. Nhóm Quản trị & Danh mục
- Users: Lưu thông tin tài khoản (UserId PK, Username UK, PasswordHash, FullName, Role, IsActive).
- Categories: Phân loại địa điểm (CategoryId PK, Name, Description).
- Languages: Quản lý ngôn ngữ (LanguageId PK, Code UK, Name, IsActive).

2. Nhóm Đối tượng cốt lõi & Tọa độ không gian (Geofencing)
- TouristSites: Lưu thông tin địa điểm và cấu hình GPS.
  + SiteId (INT, PK): Mã định danh.
  + CategoryId (INT, FK), CreatedBy (INT, FK).
  + Latitude, Longitude (DECIMAL(10,7)): Tọa độ không gian WGS84, độ chính xác đến centimet để phục vụ Geofencing.
  + TriggerRadius (INT): Bán kính kích hoạt GPS tự động phát Audio (đơn vị: mét).
  + DataVersion (INT): Cờ phiên bản để thiết bị khách kiểm tra và tải gói dữ liệu offline.
  + Status (BIT): Trạng thái hoạt động.

- QRCodes: Lưu mã QR dùng làm phương thức dự phòng khi thiết bị mất tín hiệu GPS.
  + QrId (INT, PK), SiteId (INT, FK).
  + QrCodeValue (NVARCHAR(500), UK): Mã định danh duy nhất (Có Index).

- QRScanLogs: Ghi nhận lịch sử quét.
  + ScanId (BIGINT, PK), QrId (INT, FK), ScannedAt (DATETIME2).

3. Nhóm Lưu trữ nội dung & AI Translation
- Narrations: Lưu bản thảo gốc.
  + NarrationId (INT, PK), SiteId (INT, FK).
  + OriginalContent (NVARCHAR(MAX)): Bản thảo tiếng Việt.
  + ContentHash (VARCHAR(64)): Mã băm MD5 để kiểm tra nội dung có thay đổi hay không trước khi gọi AI.

- Translations: Lưu bản dịch và âm thanh.
  + TranslationId (INT, PK), NarrationId (INT, FK), LanguageId (INT, FK). (Unique kép NarrationId + LanguageId).
  + AudioUrl (NVARCHAR(500)): Đường dẫn file âm thanh tĩnh, tuyệt đối không lưu binary trong CSDL.

- AiTranslationTasks: Quản lý hàng đợi xử lý nền.
  + TaskId (BIGINT, PK), Status (VARCHAR(20) - PENDING/PROCESSING/COMPLETED/FAILED), RetryCount (INT).

4. Nhóm Trợ lý Chatbot AI (RAG Engine)
- AiConfigurations: Cấu hình tri thức và hành vi AI.
  + ConfigId (INT, PK).
  + SystemPrompt (NVARCHAR(MAX)): Định hình "tính cách" cho trợ lý ảo.
  + BannedKeywords (NVARCHAR(MAX)): Danh sách từ khóa cấm.
  + LastSyncedAt (DATETIME2): Thời điểm đồng bộ dữ liệu TouristSites vào Vector DB/Knowledge Base.

- ChatAuditLogs: Lưu trữ lịch sử hỏi đáp.
  + LogId (BIGINT, PK).
  + UserQuestion, BotResponse (NVARCHAR(MAX)).
  + LanguageCode (VARCHAR(10)): Ngôn ngữ khách sử dụng.
  + IsFlagged (BIT): Cờ đánh dấu đoạn chat vi phạm từ khóa cấm.