# APIDesignThinking(2)

## PHẦN 1: TƯ DUY KỸ SƯ VỀ THIẾT KẾ API

Trước khi đưa code, cần hiểu tư duy thiết kế để có thể giải thích và bảo
vệ các quyết định thiết kế API.

### Tư duy 1: Tránh Under-fetching (Lấy thiếu dữ liệu) và Over-fetching (Lấy thừa dữ liệu)

**Bài toán:**

Khi khách du lịch quét mã QR, ứng dụng cần xác định địa điểm (POI), hiển
thị thông tin cơ bản của địa điểm, hình ảnh, Region tương ứng và các
ngôn ngữ thuyết minh được hỗ trợ.

**Quyết định kỹ sư:**

Không nên bắt ứng dụng gọi nhiều API riêng biệt để lấy từng trường thông
tin nếu các dữ liệu đó thuộc cùng một ngữ cảnh nghiệp vụ và cần hiển thị
cùng lúc. API tra cứu QR có thể trả về thông tin POI, Region và thông
tin ngôn ngữ cần thiết trong một phản hồi.

Tuy nhiên, không gộp dữ liệu Audio vào phản hồi này. Theo thiết kế
LinguaFlow, QR được dùng để xác định POI và suy ra Region. Sau khi biết
Region và ngôn ngữ thuyết minh mà khách đã chọn, ứng dụng gọi API riêng
để lấy danh sách Audio của toàn Region theo ngôn ngữ đó. Ứng dụng chỉ
tải các Audio đã sẵn sàng và còn hợp lệ, đồng thời bỏ qua những file đã
có phiên bản mới nhất trong bộ nhớ đệm.

**Các API minh họa:**

-   `GET /api/v1/tourist-sites/qr/{qrCodeValue}`: tra cứu POI bằng QR,
    trả về thông tin POI và Region liên quan.
-   `GET /api/v1/regions/{regionId}/audio?languageCode=ja-JP`: lấy danh
    sách Audio đã sẵn sàng của Region theo ngôn ngữ thuyết minh được
    chọn.

**Lý do thiết kế:**

Mục tiêu không phải là giảm số lượng API bằng mọi giá, mà là cân bằng
giữa số lần gọi mạng và lượng dữ liệu cần truyền. Cách tổ chức này tránh
gọi API vụn vặt để lấy thông tin POI, đồng thời tránh tải Audio không
cần thiết hoặc tải Audio của những Region mà khách chưa xác định.

**Nguồn tham khảo:**

Microsoft REST API Guidelines:
https://github.com/microsoft/api-guidelines

### Tư duy 2: Thiết kế dự phòng lỗi (Error Handling by Design)

**Bài toán:**

Khách du lịch quét một mã QR không hợp lệ, không tồn tại hoặc đã bị
Super Admin vô hiệu hóa (`IsActive = false`).

**Quyết định kỹ sư:**

API không được trả về lỗi hệ thống `500 Internal Server Error` cho những
trường hợp nghiệp vụ đã dự kiến trước. Thay vào đó, API cần trả về mã
HTTP phù hợp với tình huống và phần thân lỗi có cấu trúc để frontend có
thể hiển thị thông báo dễ hiểu.

  -------------------------------------------------------------------------
  Trường hợp                      HTTP status đề xuất Ý nghĩa
  --------------------- ----------------------------- ---------------------
  Mã QR không tồn tại                 `404 Not Found` Không tìm thấy tài
                                                      nguyên được yêu cầu

  Mã QR đã bị vô hiệu                      `410 Gone` Tài nguyên không còn
  hóa                                                 khả dụng

  Người dùng không có                 `403 Forbidden` Máy chủ hiểu yêu cầu
  quyền truy cập tài                                  nhưng từ chối cấp
  nguyên                                              quyền

  Lỗi không dự kiến       `500 Internal Server Error` Lỗi nội bộ cần được
  phía máy chủ                                        ghi nhận và xử lý
  -------------------------------------------------------------------------

Mã trạng thái cụ thể cần được áp dụng nhất quán trong toàn bộ API.
Frontend dựa vào HTTP status và nội dung lỗi để hiển thị thông báo phù
hợp; không nhận hoặc hiển thị trực tiếp Java Exception hay stack trace.

**Định dạng lỗi:**

Sử dụng cấu trúc Problem Details for HTTP APIs để chuẩn hóa phản hồi
lỗi. Ví dụ:

``` json
{
  "type": "https://example.com/problems/qr-code-disabled",
  "title": "QR code is unavailable",
  "status": 410,
  "detail": "This QR code has been disabled.",
  "instance": "/api/v1/tourist-sites/qr/ABC123"
}
```

**Nguồn tham khảo:**

RFC 9457 - Problem Details for HTTP APIs:
https://datatracker.ietf.org/doc/html/rfc9457

RFC 9457 thay thế RFC 7807 và tiếp tục chuẩn hóa định dạng Problem
Details cho HTTP API.
