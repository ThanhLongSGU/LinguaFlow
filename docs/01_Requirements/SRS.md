TÀI LIỆU ĐẶC TẢ YÊU CẦU PHẦN MỀM (SRS)
Dự án: LinguaFlow - Hệ thống thuyết minh tự động đa ngôn ngữ 
Kiến trúc: 3-Tier (React.js, Spring Boot, SQL Server)

1. GIỚI THIỆU (INTRODUCTION)
1.1. Mục đích (Purpose)
Tài liệu này đặc tả các yêu cầu chức năng và phi chức năng của hệ thống LinguaFlow, được ánh xạ trực tiếp từ Tài liệu Yêu cầu Nghiệp vụ (PRD). Tài liệu làm cơ sở cho thiết kế kiến trúc, mô hình dữ liệu (ERD) và viết Prompt cho AI sinh mã nguồn.

1.2. Phạm vi (Scope)
LinguaFlow là ứng dụng Web (PWA/Web-based) hỗ trợ khách tham quan tự động nghe thuyết minh dựa trên tọa độ vị trí (GPS) và tương tác với AI. Hệ thống chia làm 2 phân hệ:
- Phân hệ Du khách (Visitor): Đồng bộ dữ liệu offline, xem bản đồ, nghe thuyết minh tự động qua Geofencing/QR, và hỏi đáp với Trợ lý AI.
- Phân hệ Quản trị (Admin): Thống kê hiệu suất, quản lý điểm đến (POI) và cấu hình tri thức cho hệ thống AI.

2. MÔ TẢ TỔNG QUAN (OVERALL DESCRIPTION)
2.1. Phân loại người dùng (User Classes)
- Khách tham quan (Visitor): Sử dụng trình duyệt di động, cấp quyền định vị GPS, có nhu cầu tra cứu bản đồ và nghe thuyết minh tự động.
- Quản trị viên (Admin): Người có quyền cấu hình hệ thống, quản lý dữ liệu POI, duyệt bản dịch và nạp dữ liệu cho AI.
- Hệ thống AI (System Actor): Các background worker xử lý dịch thuật, sinh file Audio (TTS) và động cơ RAG trả lời Chatbot.

2.2. Ràng buộc thiết kế (Constraints)
- CON-01 (Offline-First): Hệ thống phải ưu tiên đồng bộ gói dữ liệu POI vào thiết bị (Local Storage) ở lần truy cập đầu tiên để phục vụ định vị khi mất mạng.
- CON-02 (Giới hạn tài nguyên): File Audio tải lên không vượt quá 20MB, Hình ảnh không vượt quá 5MB. SQL Server chỉ lưu đường dẫn (URL), tuyệt đối không lưu file nhị phân.
- CON-03 (Bất đồng bộ): Mọi tác vụ gọi API dịch thuật và TTS bên thứ 3 phải chạy ngầm (@Async/Task Queue).

3. YÊU CẦU CHỨC NĂNG (FUNCTIONAL REQUIREMENTS - FR)

Nhóm 3.1: Phân hệ Du khách (Visitor)
- FR-VIS-01 [UC1]: Hệ thống tải giao diện và tự động đồng bộ danh sách địa điểm (Tọa độ, Bán kính, Thông tin cơ bản) lưu vào bộ nhớ thiết bị ngay lần truy cập đầu tiên. Kiểm tra DataVersion để cập nhật nếu có bản mới.
- FR-VIS-02 [UC2]: Hệ thống cung cấp Bản đồ số (Map API), hiển thị các điểm đánh dấu (Marker) và liên tục cập nhật vị trí hiện tại (GPS) của du khách trên bản đồ.
- FR-VIS-03 [UC3]: Cho phép đổi ngôn ngữ hiển thị. Khi thay đổi, hệ thống phải tải lại giao diện (reload) và chuyển nguồn phát Audio sang ngôn ngữ tương ứng.
- FR-VIS-04 [UC4]: Chức năng Geofencing tự động đối chiếu tọa độ GPS với POI. Kích hoạt phát Audio khi khách vào vùng bán kính < 20m. Yêu cầu có cơ chế Cooldown (VD: 5 phút) để chống phát lặp lại liên tục. Hỗ trợ quét mã QR để phát Audio dự phòng khi mất GPS.
- FR-VIS-05 [UC5]: Trợ lý Chat AI cho phép khách đặt câu hỏi chuyên sâu. Hệ thống tự động đính kèm ngữ cảnh (Vị trí, Ngôn ngữ) vào Prompt. Bắt buộc từ chối trả lời nếu vi phạm từ khóa cấm.

Nhóm 3.2: Phân hệ Quản trị (Admin)
- FR-ADM-01 [UC6]: Đăng nhập qua JWT. Phiên làm việc (Session) tự động hết hạn sau 30 phút. Khóa tài khoản 15 phút nếu nhập sai quá 5 lần.
- FR-ADM-02 [UC7]: Dashboard hiển thị thống kê Real-time (số người online, phiên GPS) và dữ liệu lịch sử (biểu đồ lượt quét, lượt nghe) có khả năng lọc theo thời gian.
- FR-ADM-03 [UC8]: Quản lý POI (CRUD). Admin nhập Tọa độ Lat/Lng, Bán kính kích hoạt, Tên, Mô tả, và Upload File. Khi lưu thành công, hệ thống tăng DataVersion để kích hoạt luồng cập nhật cho Du khách (UC1).
- FR-ADM-04 [UC9]: Quản lý AI. Admin thiết lập System Prompt, danh sách từ khóa cấm (Banned Keywords) và nhấn nút đồng bộ dữ liệu POI vào Knowledge Base của AI.

Nhóm 3.3: Động cơ AI ngầm (System Engine)
- FR-AI-01: Lắng nghe sự kiện tạo/sửa Narration. Tự động dịch và gọi API Text-To-Speech ra file .mp3 theo cơ chế bất đồng bộ.
- FR-AI-02: Checksum (ContentHash MD5). Nếu nội dung bản thảo không đổi, bỏ qua việc gọi API dịch/TTS để tiết kiệm chi phí.
- FR-AI-03: Fallback ngôn ngữ. Nếu Audio ngôn ngữ khách chọn chưa có sẵn, hệ thống fallback sang tiếng Anh, tiếp tục fallback về tiếng Việt.

4. YÊU CẦU PHI CHỨC NĂNG (NON-FUNCTIONAL REQUIREMENTS - NFR)
- NFR-PF-01: Ứng dụng mô hình Cache-Aside (Redis) cho API lấy gói dữ liệu POI ban đầu, đảm bảo thời gian phản hồi < 200ms.
- NFR-RL-01: Ứng dụng Resilience4j (Retry/Circuit Breaker) cho các lệnh gọi API dịch thuật/TTS ra bên ngoài để đảm bảo khả năng tự phục hồi khi nhà cung cấp lỗi.
- NFR-SC-01: Mọi API ngoại trừ luồng quét mã QR, đồng bộ POI và API cấp Token đều phải được bảo vệ bằng Spring Security (Xác thực JWT).