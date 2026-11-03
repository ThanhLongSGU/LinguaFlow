PHẦN 1: TƯ DUY KỸ SƯ VỀ THIẾT KẾ API
Trước khi đưa code, tui sẽ trang bị cho bạn tư duy thiết kế để bạn có thể tự bảo vệ quyết định của mình trước giảng viên:
•	Tư duy 1: Tránh Under-fetching (Lấy thiếu dữ liệu) và Over-fetching (Lấy thừa dữ liệu)
o	Bài toán: Khi Khôi quét mã QR, Khôi cần hiển thị tên địa điểm, hình ảnh, và danh sách các ngôn ngữ có sẵn để khách chọn, kèm theo file audio.
o	Quyết định kỹ sư: Thay vì bắt Khôi gọi 1 API lấy thông tin địa điểm (GET /sites/1), rồi gọi thêm 1 API nữa lấy danh sách ngôn ngữ (GET /sites/1/translations), ta sẽ gộp chung vào 1 API duy nhất. Điều này giúp giảm thiểu độ trễ mạng (Latency) trên điện thoại di động của khách du lịch, đáp ứng trực tiếp yêu cầu NFR-PF-01 (Thời gian phản hồi < 200ms) trong SRS.
o	Nguồn học thuật chính: Microsoft REST API Guidelines ([github.com/microsoft/api-guidelines](https://github.com/microsoft/api-guidelines)) - Tiêu chuẩn thiết kế API của Microsoft, nhấn mạnh việc tối ưu số lượng HTTP Request cho thiết bị di động.
•	Tư duy 2: Thiết kế dự phòng lỗi (Error Handling by Design)
o	Bài toán: Nếu Huy cung cấp một mã QR đã bị vô hiệu hóa (IsActive = false).
o	Quyết định kỹ sư: API không được trả về lỗi hệ thống 500 (Internal Server Error) hay đẩy một cục Exception Java ra ngoài. API phải trả về mã 403 Forbidden kèm thông báo lỗi rõ ràng bằng JSON để Khôi hiện popup cảnh báo.
o	Nguồn học thuật chính: RFC 7807 - Problem Details for HTTP APIs (datatracker.ietf.org/doc/html/rfc7807) - Chuẩn mực quốc tế về cách định dạng cấu trúc báo lỗi trong REST API.

