flowchart TB
    ROOT["HỆ THỐNG HƯỚNG DẪN DU LỊCH ĐA NGÔN NGỮ (LINGUAFLOW)"] --> F1["1.0 PHÂN HỆ DU KHÁCH (VISITOR)"] & F2["2.0 PHÂN HỆ QUẢN TRỊ (ADMIN)"] & F3["3.0 ĐỘNG CƠ XỬ LÝ (SYSTEM ENGINE)"]

    %% 1. Phân hệ Khách (Visitor)
    F1 --> F11["1.1 UC1: Truy cập & Đồng bộ Dữ liệu Offline"]
    F1 --> F12["1.2 UC2: Xem Bản đồ & Vị trí"]
    F1 --> F13["1.3 UC3: Chọn Ngôn ngữ"]
    F1 --> F14["1.4 UC4: Nghe Thuyết minh (GPS & QR)"]
    F1 --> F15["1.5 UC5: Hỏi Chat AI"]

    F11 --> F111["Kiểm tra DataVersion & Tải gói POI cục bộ"]
    F12 --> F121["Hiển thị Marker POI & Tọa độ GPS hiện tại"]
    F14 --> F141["Geofence quét bán kính kích hoạt ( mặc định < 20m )"]
    F14 --> F142["Cơ chế Debounce & Cooldown chống phát lặp"]
    F14 --> F143["Quét mã QR kích hoạt dự phòng khi mất GPS"]
    F15 --> F151["Gửi câu hỏi đính kèm ngữ cảnh (Vị trí, Ngôn ngữ)"]
    F15 --> F152["Truy xuất lịch sử hội thoại cục bộ"]

    %% 2. Phân hệ Quản trị (Admin)
    F2 --> F21["2.1 UC6: Đăng nhập"]
    F2 --> F22["2.2 UC7: Xem Thống kê (Dashboard)"]
    F2 --> F23["2.3 UC8: Quản lý Nội dung POI"]
    F2 --> F24["2.4 UC9: Quản lý Hệ thống AI"]

    F21 --> F211["Xác thực JWT & Quản lý Session 30 phút"]
    F22 --> F221["Thống kê Real-time & Biểu đồ lịch sử"]
    F23 --> F231["CRUD POI (Tọa độ, Bán kính, File Audio/Ảnh)"]
    F23 --> F232["Đánh dấu DataVersion mới khi cập nhật"]
    F24 --> F241["Thiết lập System Prompt & Banned Keywords"]
    F24 --> F242["Đồng bộ tri thức POI vào Knowledge Base"]

    %% 3. Động cơ xử lý (System Engine)
    F3 --> F31["3.1 Dịch thuật tự động đa ngôn ngữ (@Async)"]
    F3 --> F32["3.2 Tổng hợp giọng nói Text-To-Speech (@Async)"]
    F3 --> F33["3.3 RAG Engine xử lý câu hỏi Chatbot AI"]

    classDef rootStyle fill:#1e293b,stroke:#0f172a,stroke-width:2px,color:#fff,font-weight:bold
    classDef visitorStyle fill:#dcfce7,stroke:#10b981,stroke-width:1.5px,color:#065f46
    classDef adminStyle fill:#fee2e2,stroke:#ef4444,stroke-width:1.5px,color:#991b1b
    classDef sysStyle fill:#e0e7ff,stroke:#6366f1,stroke-width:1.5px,color:#3730a3
    
    ROOT:::rootStyle
    F1:::visitorStyle
    F11:::visitorStyle
    F12:::visitorStyle
    F13:::visitorStyle
    F14:::visitorStyle
    F15:::visitorStyle
    F2:::adminStyle
    F21:::adminStyle
    F22:::adminStyle
    F23:::adminStyle
    F24:::adminStyle
    F3:::sysStyle
    F31:::sysStyle
    F32:::sysStyle
    F33:::sysStyle