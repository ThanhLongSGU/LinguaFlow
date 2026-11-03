sequenceDiagram
autonumber
actor Visitor as Du khách (Trình duyệt)
participant App as Client Engine (React/PWA)
participant Ctrl as SyncController
participant Svc as SiteSyncService
participant Redis as Redis Cache
participant DB as SQL Server

    Note over Visitor, DB: 1. ĐỒNG BỘ DỮ LIỆU OFFLINE (UC1)
    Visitor->>App: Truy cập ứng dụng
    activate App
    App->>App: Kiểm tra DataVersion cục bộ (IndexedDB)
    App->>Ctrl: GET /api/v1/sync/poi?clientVersion={v}
    activate Ctrl

    Ctrl->>Svc: fetchPOIPackage()
    activate Svc
    Svc->>Redis: GET "poi_package_latest"
    activate Redis

    alt Cache Hit (Có sẵn trên RAM)
        Redis-->>Svc: POIPackageDTO
    else Cache Miss (Hết hạn hoặc bị xóa)
        Svc->>DB: Truy vấn TouristSites (Lat, Lng, Radius, AudioUrl)
        DB-->>Svc: Dữ liệu POI
        Svc->>Redis: SET "poi_package_latest" (TTL: 1h)
    end
    deactivate Redis

    Svc-->>Ctrl: Dữ liệu POIPackageDTO
    deactivate Svc
    Ctrl-->>App: HTTP 200 OK (Gói POI mới)
    deactivate Ctrl

    App->>App: Lưu gói POI vào LocalStorage/IndexedDB
    deactivate App

    Note over Visitor, App: 2. KÍCH HOẠT GEOFENCING (UC4)
    loop Background Location (Mỗi 3 giây)
        Visitor->>App: Tọa độ GPS hiện tại (Lat, Lng)
        activate App
        App->>App: Tính khoảng cách Haversine tới các POI offline
        alt Khoảng cách <= TriggerRadius (VD: 20m)
            App->>App: Kiểm tra Cooldown (5 phút)
            alt Thỏa điều kiện (Không bị spam)
                App->>Visitor: Tự động phát Audio thuyết minh
            else Trùng thời gian Cooldown
                App->>App: Bỏ qua (Chống lặp)
            end
        end
        deactivate App
    end
