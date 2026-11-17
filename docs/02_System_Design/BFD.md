flowchart TB
ROOT["HỆ THỐNG HƯỚNG DẪN DU LỊCH ĐA NGÔN NGỮ (LINGUAFLOW)"]

    ROOT --> F1["1.0 PHÂN HỆ DU KHÁCH (VISITOR)"]
    ROOT --> F2["2.0 PHÂN HỆ QUẢN TRỊ (ADMIN)"]
    ROOT --> F3["3.0 ĐỘNG CƠ XỬ LÝ (SYSTEM ENGINE)"]

    %% 1. PHÂN HỆ DU KHÁCH
    F1 --> F11["1.1 UC01: Truy cập và Đồng bộ Dữ liệu"]
    F1 --> F12["1.2 UC02: Xem Bản đồ và Vị trí"]
    F1 --> F13["1.3 UC03: Chọn Ngôn ngữ"]
    F1 --> F14["1.4 UC04: Nghe Thuyết minh (GPS và QR)"]
    F1 --> F15["1.5 UC05: Hỏi Chat AI"]

    %% UC01
    F11 --> F111["Kiểm tra dữ liệu POI cục bộ"]
    F11 --> F112["Kiểm tra DataVersion bằng clientVersion"]
    F11 --> F113["Tải và lưu dữ liệu POI cơ bản khi có bản cập nhật"]
    F11 --> F114["Sử dụng dữ liệu cũ khi HTTP 304"]
    F11 --> F115["Yêu cầu kết nối mạng trong lần truy cập đầu tiên"]
    F11 --> F116["Lưu thông tin phân khu tương ứng với POI"]
    F11 --> F117["Xác định phân khu bằng GPS hoặc QR"]
    F11 --> F118["Tải toàn bộ Audio của phân khu theo ngôn ngữ đã chọn"]
    F11 --> F119["Lưu Audio phân khu vào bộ nhớ đệm cục bộ"]

    %% UC02
    F12 --> F121["Hiển thị Marker POI"]
    F12 --> F122["Hiển thị vị trí GPS hiện tại"]
    F12 --> F123["Hiển thị bán kính Geofence của từng POI"]
    F12 --> F124["Xem thông tin chi tiết POI từ dữ liệu cục bộ"]

    %% UC03
    F13 --> F131["Hiển thị danh sách ngôn ngữ hỗ trợ"]
    F13 --> F132["Cập nhật toàn bộ giao diện theo ngôn ngữ được chọn"]
    F13 --> F133["Cập nhật ngôn ngữ nội dung thuyết minh và Audio"]
    F13 --> F134["Tải Audio của phân khu theo ngôn ngữ mới"]
    F13 --> F135["Giữ dữ liệu cache hợp lệ khi không tải được tài nguyên mới"]

    %% UC04
    F14 --> F141["Tính khoảng cách Haversine hoàn toàn phía Client"]
    F14 --> F142["Lọc tín hiệu GPS có sai số lớn hơn 150m"]
    F14 --> F143["Áp dụng hysteresis 40m"]
    F14 --> F144["Đối chiếu vị trí với bán kính Geofence của từng POI"]
    F14 --> F145["Kiểm tra hàng chờ âm thanh để tránh phát chồng chéo"]
    F14 --> F146["Cooldown 5 phút cho từng POI phát tự động qua GPS"]
    F14 --> F147["Quét QR để kích hoạt phát thuyết minh"]
    F14 --> F148["Chọn POI trên bản đồ để kích hoạt phát thuyết minh"]
    F14 --> F149["Ưu tiên QR và chọn POI trên bản đồ hơn GPS"]
    F14 --> F1410["GPS xác định phân khu và kích hoạt tải Audio toàn phân khu"]
    F14 --> F1411["QR xác định POI và suy ra phân khu tương ứng"]
    F14 --> F1412["Tự động tải Audio toàn phân khu sau khi quét QR"]
    F14 --> F1413["Kiểm tra cache và chỉ tải Audio còn thiếu hoặc đã thay đổi"]

    %% UC05
    F15 --> F151["Tiếp nhận câu hỏi của du khách"]
    F15 --> F152["Gửi câu hỏi kèm ngữ cảnh vị trí và ngôn ngữ"]
    F15 --> F153["Yêu cầu AI trả lời bằng ngôn ngữ du khách đã chọn"]
    F15 --> F154["Kiểm tra từ khóa cấm và phạm vi nội dung"]
    F15 --> F155["Hiển thị câu trả lời hoặc phản hồi từ chối"]
    F15 --> F156["Lưu và truy xuất lịch sử hội thoại cục bộ"]

    %% 2. PHÂN HỆ QUẢN TRỊ
    F2 --> F21["2.1 UC06: Đăng nhập và Quản lý Tài khoản"]
    F2 --> F22["2.2 UC07: Xem Thống kê Dashboard"]
    F2 --> F23["2.3 UC08: Quản lý Nội dung POI"]
    F2 --> F24["2.4 UC09: Quản lý Hệ thống AI"]

    %% UC06
    F21 --> F211["Khởi tạo duy nhất tài khoản Super Admin admin"]
    F21 --> F212["Xác thực thông tin đăng nhập và cấp JWT"]
    F21 --> F213["Đổi mật khẩu Super Admin"]
    F21 --> F214["Hết hạn phiên sau 30 phút không có thao tác"]
    F21 --> F215["Khóa đăng nhập 15 phút sau hơn 5 lần sai mật khẩu liên tiếp"]

    %% UC07
    F22 --> F221["Thống kê số phiên truy cập website đang hoạt động"]
    F22 --> F222["Thống kê số phiên GPS đang hoạt động"]
    F22 --> F223["Thống kê lịch sử lượt nghe Audio và lượt quét QR"]
    F22 --> F224["Thống kê hoạt động Chat AI và POI được quan tâm"]
    F22 --> F225["Thống kê phản hồi do du khách tự nguyện gửi"]
    F22 --> F226["Lọc dữ liệu thống kê theo thời gian"]

    %% UC08
    F23 --> F231["Thêm, xem, cập nhật và xóa POI"]
    F23 --> F232["Quản lý tọa độ, phân khu, bán kính, tên và mô tả POI"]
    F23 --> F233["Quản lý hình ảnh POI với dung lượng tối đa 5MB"]
    F23 --> F234["Quản lý một bài thuyết minh cho mỗi POI"]
    F23 --> F235["Biên soạn nội dung tiếng Việt và tiếng Anh Mỹ"]
    F23 --> F236["Bật hoặc tắt ngôn ngữ bổ sung theo từng POI"]
    F23 --> F237["Kiểm tra và duyệt nội dung dịch AI"]
    F23 --> F238["Theo dõi trạng thái dịch thuật và trạng thái Audio"]
    F23 --> F239["Tăng DataVersion khi dữ liệu thuộc gói đồng bộ thay đổi"]

    %% UC09
    F24 --> F241["Thiết lập System Prompt"]
    F24 --> F242["Quản lý danh sách Banned Keywords"]
    F24 --> F243["Đồng bộ dữ liệu POI vào Knowledge Base"]
    F24 --> F244["Cập nhật cấu hình cho AI Engine"]

    %% 3. ĐỘNG CƠ XỬ LÝ
    F3 --> F31["3.1 Xử lý dịch thuật đa ngôn ngữ"]
    F3 --> F32["3.2 Tổng hợp giọng nói Text-To-Speech"]
    F3 --> F33["3.3 Xử lý câu hỏi Chatbot AI"]

    %% Translation Engine
    F31 --> F311["Tiếp nhận nội dung tiếng Anh Mỹ en-US làm nguồn dịch"]
    F31 --> F312["Dịch sang ngôn ngữ bổ sung được bật cho POI"]
    F31 --> F313["Lưu bản dịch tự động vào cơ sở dữ liệu"]
    F31 --> F314["Chờ Admin kiểm tra và duyệt trong quy trình quản trị"]
    F31 --> F315["Tự dịch khi du khách yêu cầu nội dung chưa tồn tại"]
    F31 --> F316["Trả nội dung en-US nếu dịch theo yêu cầu thất bại"]

    %% TTS Engine
    F32 --> F321["Tiếp nhận nội dung đã đủ điều kiện tạo Audio"]
    F32 --> F322["Tạo tệp Audio bằng TTS theo từng ngôn ngữ"]
    F32 --> F323["Lưu Audio trên máy chủ hoặc dịch vụ lưu trữ tệp"]
    F32 --> F324["Cập nhật trạng thái và phiên bản Audio độc lập"]
    F32 --> F325["Đưa Audio mới hoặc cần tạo lại vào hàng đợi xử lý nền"]

    %% Chatbot Engine
    F33 --> F331["Tiếp nhận câu hỏi và ngữ cảnh"]
    F33 --> F332["Truy xuất tri thức POI từ Knowledge Base"]
    F33 --> F333["Sinh câu trả lời theo ngôn ngữ được yêu cầu"]
    F33 --> F334["Kiểm tra chính sách và từ khóa cấm trước khi phản hồi"]

    %% STYLES
    classDef rootStyle fill:#1e293b,stroke:#0f172a,stroke-width:2px,color:#fff
    classDef visitorStyle fill:#dcfce7,stroke:#10b981,stroke-width:1.5px,color:#065f46
    classDef adminStyle fill:#fee2e2,stroke:#ef4444,stroke-width:1.5px,color:#991b1b
    classDef sysStyle fill:#e0e7ff,stroke:#6366f1,stroke-width:1.5px,color:#3730a3

    class ROOT rootStyle
    class F1,F11,F12,F13,F14,F15 visitorStyle
    class F2,F21,F22,F23,F24 adminStyle
    class F3,F31,F32,F33 sysStyle
