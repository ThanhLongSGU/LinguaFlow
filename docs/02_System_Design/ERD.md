erDiagram
    %% 1. QUẢN TRỊ & HỆ THỐNG
    Users {
        INT UserId PK
        NVARCHAR(50) Username UK
        NVARCHAR(255) PasswordHash
        NVARCHAR(100) FullName
        NVARCHAR(20) Role "ROLE_ADMIN, ROLE_CONTENT"
        BIT IsActive
    }
    Categories {
        INT CategoryId PK
        NVARCHAR(100) Name
        NVARCHAR(MAX) Description
    }
    Languages {
        INT LanguageId PK
        VARCHAR(10) Code UK "vi, en, ja, ko"
        NVARCHAR(50) Name
        BIT IsActive
    }

    %% 2. POI (ĐỊA ĐIỂM THAM QUAN) & TỌA ĐỘ GPS
    TouristSites {
        INT SiteId PK
        INT CategoryId FK
        INT CreatedBy FK
        NVARCHAR(200) Name
        NVARCHAR(MAX) Description
        NVARCHAR(255) Address
        DECIMAL(10_7) Latitude "Tọa độ vĩ độ cho Geofence"
        DECIMAL(10_7) Longitude "Tọa độ kinh độ cho Geofence"
        INT TriggerRadius "Bán kính kích hoạt GPS (m), mặc định 20"
        NVARCHAR(500) ImageUrl
        DECIMAL(12_2) ReferenceTicketPrice
        BIT Status
        INT DataVersion "Phiên bản dữ liệu đồng bộ Client"
        DATETIME2 UpdatedAt
    }

    QRCodes {
        INT QrId PK
        INT SiteId FK
        NVARCHAR(500) QrCodeValue UK "Mã QR dùng cho dự phòng khi mất GPS"
        BIT IsActive
        DATETIME2 CreatedAt
    }

    QRScanLogs {
        BIGINT ScanId PK
        INT QrId FK
        DATETIME2 ScannedAt
    }

    %% 3. BÀI THUYẾT MINH & XỬ LÝ DỊCH/TTS
    Narrations {
        INT NarrationId PK
        INT SiteId FK
        NVARCHAR(200) Title
        NVARCHAR(MAX) OriginalContent "Bản thảo tiếng Việt"
        VARCHAR(64) ContentHash "MD5 kiểm tra trùng nội dung"
        DATETIME2 CreatedAt
    }

    Translations {
        INT TranslationId PK
        INT NarrationId FK
        INT LanguageId FK
        NVARCHAR(MAX) Content "Nội dung đã dịch"
        NVARCHAR(500) AudioUrl "File mp3 lưu ở /static/audio/"
    }

    AiTranslationTasks {
        BIGINT TaskId PK
        INT NarrationId FK
        INT LanguageId FK
        VARCHAR(20) Status "PENDING, PROCESSING, COMPLETED, FAILED"
        NVARCHAR(MAX) ErrorMessage
        INT RetryCount
        DATETIME2 CreatedAt
        DATETIME2 UpdatedAt
    }

    %% 4. PHÂN HỆ CHAT AI & THIẾT LẬP HỆ THỐNG
    AiConfigurations {
        INT ConfigId PK
        NVARCHAR(MAX) SystemPrompt "Thiết lập phong cách hướng dẫn viên"
        NVARCHAR(MAX) BannedKeywords "Danh sách từ cấm ngăn chặn vi phạm"
        DATETIME2 LastSyncedAt "Thời điểm đồng bộ POI vào Knowledge Base"
        INT UpdatedBy FK
    }

    ChatAuditLogs {
        BIGINT LogId PK
        NVARCHAR(MAX) UserQuestion
        NVARCHAR(MAX) BotResponse
        VARCHAR(10) LanguageCode
        BIT IsFlagged "Đánh dấu nếu vi phạm từ cấm"
        DATETIME2 CreatedAt
    }

    %% QUAN HỆ
    Categories ||--o{ TouristSites : "has"
    Users ||--o{ TouristSites : "creates"
    TouristSites ||--o{ QRCodes : "fallback_qr"
    QRCodes ||--o{ QRScanLogs : "tracks"
    TouristSites ||--o{ Narrations : "has"
    Narrations ||--o{ Translations : "translated_into"
    Languages ||--o{ Translations : "used_in"
    Narrations ||--o{ AiTranslationTasks : "triggers"
    Languages ||--o{ AiTranslationTasks : "target_for"
    Users ||--o{ AiConfigurations : "configures"