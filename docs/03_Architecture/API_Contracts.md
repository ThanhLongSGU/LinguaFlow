# LINGUAFLOW API CONTRACTS

> **Phiên bản đề xuất:** API_Contracts
> **Trạng thái:** bản đã được duyệt, có thể phát triển thêm trong tương lai
> **Căn cứ:** PRD, SRS, BFD, ERD, DataDictionary, AI_Microservice_Setup, AI_SequenceDiagram và CACHE_ASIDE_SequenceDiagram.  
> **Phạm vi:** Hợp đồng giao tiếp giữa React/PWA Client và Spring Boot Backend. Các endpoint dưới đây là đề xuất API ở tầng ứng dụng; không phải endpoint nội bộ của Python AI Microservice.

---

## 1. Quy ước chung

### 1.1. Base URL và phiên bản

Base URL minh họa khi chạy local:

```text
http://localhost:8080/api/v1
```

Các endpoint được chia thành:

- **Public/Visitor API:** phục vụ khách tham quan, không yêu cầu tài khoản Super Admin.
- **Admin API:** bắt buộc xác thực Super Admin bằng JWT.
- **Internal AI API:** không thuộc tài liệu này; được mô tả trong `AI_Microservice_Setup(2).md`.

Frontend chỉ gọi Spring Boot Backend. Frontend không gọi trực tiếp Python AI Microservice hoặc nhà cung cấp dịch thuật/TTS.

### 1.2. Kiểu dữ liệu và quy ước

- JSON sử dụng UTF-8.
- Thời gian dùng ISO 8601, ưu tiên UTC, ví dụ `2026-11-17T03:00:00Z`.
- ID số nguyên trả dưới dạng JSON number; `SessionId` là UUID.
- Ngôn ngữ dùng đúng locale code trong bảng `Languages`: `vi-VN`, `en-US`, `zh-CN`, `ja-JP`, `hi-IN`, `fr-FR`, `ar`.
- `languageCode` của nội dung thuyết minh không đồng nhất với ngôn ngữ giao diện UI.
- Các trường URL/đường dẫn là giá trị tham chiếu; Client không được tự giả định mọi URL đều công khai hoặc tồn tại vĩnh viễn.
- Các ví dụ ID, version, URL và nội dung chỉ mang tính minh họa.

### 1.3. Phân quyền

| Nhóm API                                                                 | Quyền truy cập          |
| ------------------------------------------------------------------------ | ----------------------- |
| Đồng bộ POI, tra cứu QR, lấy Audio Region, lấy nội dung thuyết minh      | Visitor/Public          |
| Tạo và quản lý phiên Visitor, heartbeat, gửi Chat Feedback               | Visitor/Public          |
| Đăng nhập Super Admin                                                    | Public                  |
| Dashboard, quản lý POI/Region/QR/ngôn ngữ, duyệt Translation, quản lý AI | Super Admin             |
| Đổi mật khẩu                                                             | Super Admin đã xác thực |

Mọi API Admin phải kiểm tra JWT và trạng thái tài khoản ở Backend. Không dựa vào việc ẩn nút trên Frontend để bảo vệ API.

---

## 2. Đồng bộ dữ liệu POI ban đầu

### 2.1. Kiểm tra phiên bản và tải gói POI

`GET /api/v1/sync/poi?clientVersion={version}`

**Mục đích:** Đồng bộ dữ liệu POI cơ bản để Client lưu IndexedDB và thực hiện bản đồ/Geofencing offline.

Query parameters:

| Tên             | Kiểu     | Bắt buộc | Mô tả                                                                        |
| --------------- | -------- | -------: | ---------------------------------------------------------------------------- |
| `clientVersion` | `BIGINT` |    Không | DataVersion hiện có ở Client. Bỏ trống hoặc `0` nếu Client chưa có snapshot. |

**Response 200 OK** — có dữ liệu mới:

```json
{
  "serverDataVersion": 12,
  "isUpdated": true,
  "generatedAt": "2026-11-17T03:00:00Z",
  "poiList": [
    {
      "siteId": 1,
      "name": "Dinh Độc Lập",
      "categoryId": 2,
      "regionId": 1,
      "description": "Di tích lịch sử tại TP. Hồ Chí Minh.",
      "address": "Quận 1, TP. Hồ Chí Minh",
      "latitude": 10.776889,
      "longitude": 106.695333,
      "triggerRadius": 20,
      "imageUrl": "/assets/images/dinh-doc-lap.jpg",
      "referenceTicketPrice": 40000,
      "status": true
    }
  ],
  "regions": [
    {
      "regionId": 1,
      "name": "Trung tâm TP. Hồ Chí Minh"
    }
  ]
}
```

**Response 304 Not Modified:** Nếu `clientVersion` bằng `serverDataVersion`, Backend trả HTTP 304 và không trả body. Client giữ snapshot hiện tại.

**Quy tắc:**

- Gói ban đầu chứa dữ liệu cơ bản cần cho bản đồ, Geofencing và xác định Region.
- Mỗi POI phải có `regionId` theo mô hình hiện tại, vì mỗi POI thuộc đúng một Region.
- Không nhúng toàn bộ `translations`, nội dung Narration hoặc `audioUrl` vào gói đồng bộ ban đầu.
- Không tải Audio của tất cả Region trong lần truy cập đầu tiên.
- Client lưu gói POI và `serverDataVersion` vào IndexedDB.
- Backend chỉ tăng `DataVersion` khi dữ liệu thuộc phạm vi gói POI sync thay đổi theo PRD. Việc tạo TTS hoặc cập nhật trạng thái Audio không tự tăng version.
- Nếu Redis không khả dụng, Backend vẫn phải có thể đọc SQL Server trực tiếp; Redis không phải điều kiện bắt buộc để API hoạt động.

### 2.2. Quy tắc Cache Aside

Cache key đề xuất:

```text
poi-package:{serverDataVersion}
```

Backend xác định version hiện hành trước, sau đó lấy gói dữ liệu tương ứng từ Redis. Cache miss thì truy vấn SQL Server, tạo DTO và ghi cache. Không dùng cache của version cũ để trả như dữ liệu mới.

---

## 3. Xác định POI bằng QR

### 3.1. Tra cứu QR

`GET /api/v1/tourist-sites/qr/{qrCodeValue}`

**Mục đích:** Xác định POI từ mã QR; sau đó trả `regionId` để Client tải Audio theo Region. Không yêu cầu quyền GPS.

**Response 200 OK:**

```json
{
  "siteId": 1,
  "name": "Dinh Độc Lập",
  "regionId": 1,
  "categoryId": 2,
  "latitude": 10.776889,
  "longitude": 106.695333,
  "narrationId": 15,
  "status": true
}
```

**Lỗi có thể gặp:**

- `404 Not Found`: Không tồn tại QR hoặc QR không còn hiệu lực.
- `400 Bad Request`: Giá trị QR không hợp lệ.

**Quy tắc:**

- Chỉ chấp nhận QR đang `IsActive = true`.
- Backend tra `QRCodes` để tìm `SiteId`, sau đó lấy `TouristSites.RegionId`.
- QR là phương thức xác định POI/Region độc lập với GPS.
- Nếu cần ghi nhận lượt quét, Backend gắn sự kiện với `SessionId` hợp lệ và ghi `QRScanLogs`; không lưu lịch sử tọa độ GPS.

---

## 4. Tải Audio theo Region và ngôn ngữ thuyết minh

### 4.1. Lấy Audio Manifest

`GET /api/v1/regions/{regionId}/audio?languageCode={code}`

**Mục đích:** Trả danh sách Audio sẵn sàng thuộc Region được xác định theo GPS hoặc QR, theo ngôn ngữ thuyết minh khách đang chọn.

Query parameters:

| Tên            | Kiểu     | Bắt buộc | Mô tả                              |
| -------------- | -------- | -------: | ---------------------------------- |
| `languageCode` | `string` |       Có | Locale thuyết minh, ví dụ `ja-JP`. |

**Response 200 OK:**

```json
{
  "regionId": 1,
  "languageCode": "ja-JP",
  "generatedAt": "2026-11-17T03:05:00Z",
  "items": [
    {
      "audioId": 101,
      "siteId": 1,
      "translationId": 301,
      "languageCode": "ja-JP",
      "audioVersion": 3,
      "contentHash": "sha256-content-hash",
      "audioUrl": "https://storage.example/audio/1/ja-JP/hash.mp3",
      "fileSize": 245760
    }
  ]
}
```

`items` có thể là mảng rỗng nếu Region chưa có Audio khả dụng cho ngôn ngữ đó.

**Quy tắc:**

- Chỉ trả Audio có `Status = READY`, hash còn khớp Translation hiện hành và nội dung đủ điều kiện phục vụ khách.
- Với bản dịch AI trong quy trình quản trị, chỉ bản dịch đã được duyệt mới đủ điều kiện phục vụ.
- Bản dịch `AI_ON_DEMAND` được xử lý theo quy tắc riêng tại mục 5.
- Không trả Audio `PENDING`, `PROCESSING`, `FAILED` hoặc `STALE`.
- Không trả Audio của Region khác.
- Không trả Audio của ngôn ngữ khác dưới nhãn `languageCode` đang yêu cầu.
- `audioVersion` và `contentHash` cho phép Client kiểm tra cache. Client không dùng URL ký có thời hạn làm định danh duy nhất của file.
- Nếu Storage sử dụng URL ký có thời hạn, Backend cấp URL truy cập phù hợp; khi URL hết hạn, Client có thể lấy manifest/URL mới mà không phải tải lại file nếu cache vẫn hợp lệ.

### 4.2. Tải một tệp Audio

`GET /api/v1/audio/{audioId}/content`

**Mục đích:** Tải binary Audio nếu Backend đóng vai trò proxy. Nếu triển khai Storage URL trực tiếp hoặc URL ký, manifest có thể trả URL tải trực tiếp thay cho việc đi qua endpoint này.

**Response 200 OK:** Binary audio, ví dụ `Content-Type: audio/mpeg`.

**Quy tắc:**

- Chỉ phục vụ Audio đang `READY` và hash hợp lệ.
- Không phục vụ Audio `STALE`.
- Có thể hỗ trợ `ETag` hoặc `Last-Modified` để tận dụng cache HTTP.
- Không bắt buộc dùng endpoint proxy nếu hạ tầng Object Storage cung cấp URL tải an toàn.

### 4.3. Fallback Audio

- Nếu Audio của ngôn ngữ được chọn chưa sẵn sàng, Backend có thể cung cấp Audio `en-US` làm phương án dự phòng nếu có sẵn, theo SRS.
- Response phải thể hiện rõ `requestedLanguageCode` và `actualLanguageCode`; không gắn nhãn Audio `en-US` thành Audio của ngôn ngữ khách yêu cầu.
- Nếu không có Audio phù hợp hoặc Audio fallback, trả trạng thái chưa sẵn sàng; Client thông báo cho khách và không phát Audio cũ/sai ngôn ngữ.
- Việc tạo TTS không được chạy đồng bộ trong request phát thuyết minh.

---

## 5. Lấy nội dung thuyết minh theo POI và ngôn ngữ

### 5.1. Lấy nội dung Narration

`GET /api/v1/tourist-sites/{siteId}/narration?languageCode={code}`

**Mục đích:** Lấy nội dung văn bản cho một POI theo ngôn ngữ thuyết minh. API này không đồng nhất với việc tải Audio của toàn Region.

**Response 200 OK — bản dịch đã có:**

```json
{
  "siteId": 1,
  "narrationId": 15,
  "requestedLanguageCode": "ja-JP",
  "actualLanguageCode": "ja-JP",
  "translationId": 301,
  "content": "統一会堂はホーチミン市にある歴史的な建物です。",
  "sourceType": "AI_ADMIN",
  "reviewStatus": "APPROVED",
  "contentVersion": 2
}
```

**Trường hợp chưa có bản dịch:** Nếu ngôn ngữ được yêu cầu chưa có Translation khả dụng, Backend có thể kích hoạt luồng `AI_ON_DEMAND` từ `en-US` như mục 5.2.

### 5.2. Dịch on-demand khi khách yêu cầu ngôn ngữ chưa có

Đây là hành vi nghiệp vụ của API lấy Narration; không bắt buộc phải cung cấp một endpoint riêng cho Client gọi dịch trực tiếp.

Quy tắc:

1. Backend tìm Translation của ngôn ngữ yêu cầu.
2. Nếu đã có bản dịch hiện hành và khả dụng, trả ngay.
3. Nếu chưa có, Backend lấy nội dung `en-US` và gọi AI Microservice để dịch.
4. Nếu dịch thành công, Backend lưu Translation với `SourceType = AI_ON_DEMAND`, `ReviewStatus = NOT_REQUIRED`, sau đó trả nội dung vừa dịch.
5. Backend tạo tác vụ TTS riêng, không chặn response nội dung để chờ Audio.
6. Nếu dịch thất bại, trả nội dung `en-US` làm fallback nếu có.
7. Nếu không có cả nội dung ngôn ngữ yêu cầu lẫn nội dung `en-US`, trả lỗi phù hợp.

**Response 200 OK — fallback về English US:**

```json
{
  "siteId": 1,
  "narrationId": 15,
  "requestedLanguageCode": "ja-JP",
  "actualLanguageCode": "en-US",
  "translationId": 302,
  "content": "The Independence Palace is a historic building in Ho Chi Minh City.",
  "sourceType": "MANUAL",
  "reviewStatus": "NOT_REQUIRED",
  "fallbackUsed": true,
  "fallbackReason": "TRANSLATION_UNAVAILABLE"
}
```

**Lưu ý:** Khi dịch thất bại, Backend phải ghi nhận lỗi/trạng thái tác vụ để có thể thử lại. Không trả bản dịch cũ có `SourceContentHash` không khớp với nội dung `en-US` hiện tại.

---

## 6. Quản lý phiên Visitor và heartbeat

### 6.1. Tạo phiên truy cập

`POST /api/v1/visitor-sessions`

**Mục đích:** Tạo phiên truy cập ẩn danh để thống kê Online Visitors và các sự kiện sử dụng.

Request:

```json
{
  "clientStartedAt": "2026-11-17T03:00:00Z"
}
```

Response `201 Created`:

```json
{
  "sessionId": "c3d2f4c0-19a2-4b4a-9e31-5a1b7e6a0012",
  "startedAt": "2026-11-17T03:00:01Z"
}
```

### 6.2. Heartbeat website

`POST /api/v1/visitor-sessions/{sessionId}/heartbeat`

Request:

```json
{
  "occurredAt": "2026-11-17T03:01:00Z"
}
```

Response `204 No Content`.

Backend cập nhật `LastHeartbeatAt`. Heartbeat website không đồng nghĩa với heartbeat GPS.

### 6.3. Heartbeat GPS

`POST /api/v1/visitor-sessions/{sessionId}/gps-heartbeat`

Request:

```json
{
  "occurredAt": "2026-11-17T03:01:05Z"
}
```

Response `204 No Content`.

Backend chỉ cập nhật `LastGpsHeartbeatAt` khi Client đã cấp quyền GPS và thực sự gửi heartbeat GPS. Không cần gửi tọa độ GPS vào API này nếu mục tiêu chỉ là xác định phiên GPS đang hoạt động.

### 6.4. Kết thúc phiên

`POST /api/v1/visitor-sessions/{sessionId}/end`

Response `204 No Content`.

**Quy tắc chung:**

- Session là ẩn danh, không yêu cầu tài khoản khách.
- Không lưu tọa độ GPS hoặc lịch sử di chuyển trong `VisitorSessions`.
- Ngưỡng xác định phiên Online/GPS Active được cấu hình ở Server, không cố định trong API Contract.
- Session không hợp lệ hoặc không tồn tại trả `404 Not Found` hoặc `410 Gone` theo chính sách triển khai.
- Không tin cậy thời gian Client như nguồn thời gian duy nhất; Backend có thể dùng thời gian Server làm thời điểm ghi nhận chính thức.

---

## 7. Ghi nhận lượt nghe Audio và quét QR

### 7.1. Ghi nhận lượt nghe

`POST /api/v1/audio-listens`

Request:

```json
{
  "audioId": 101,
  "siteId": 1,
  "sessionId": "c3d2f4c0-19a2-4b4a-9e31-5a1b7e6a0012",
  "playedAt": "2026-11-17T03:02:00Z"
}
```

Response `201 Created`:

```json
{
  "listenId": 9001,
  "recordedAt": "2026-11-17T03:02:01Z"
}
```

Backend kiểm tra các ID liên quan hợp lệ trước khi ghi `AudioListenLogs`. Đây là sự kiện thống kê, không phải bằng chứng xác thực danh tính người nghe.

### 7.2. Ghi nhận lượt quét QR

Việc tra QR được thực hiện bằng `GET /api/v1/tourist-sites/qr/{qrCodeValue}` tại mục 3. Sau khi xác định QR, Backend có thể ghi `QRScanLogs` gắn với Visitor Session.

Có thể triển khai ghi log ngay trong quá trình tra QR hoặc bằng endpoint riêng:

`POST /api/v1/qr-scan-logs`

Request:

```json
{
  "qrCodeValue": "LINGUAFLOW-POI-001",
  "sessionId": "c3d2f4c0-19a2-4b4a-9e31-5a1b7e6a0012"
}
```

Response `201 Created`:

```json
{
  "scanId": 7001,
  "siteId": 1,
  "regionId": 1,
  "scannedAt": "2026-11-17T03:03:00Z"
}
```

Nếu triển khai ghi log ngay trong API tra QR thì không cần tạo endpoint ghi log riêng; tránh ghi hai lần cho cùng một lượt quét.

---

## 8. Chat AI, thống kê và phản hồi tự nguyện

### 8.1. Gửi câu hỏi Chat AI

`POST /api/v1/chat/messages`

Request:

```json
{
  "question": "What historical sites should I visit nearby?",
  "languageCode": "en-US",
  "context": {
    "currentSiteId": 1,
    "currentRegionId": 1
  }
}
```

Response `200 OK`:

```json
{
  "answer": "You can start with the Independence Palace...",
  "languageCode": "en-US",
  "blocked": false
}
```

**Quy tắc:**

- Ngôn ngữ phản hồi theo `languageCode` khách chọn.
- Context địa điểm/Region được gửi khi có và cần thiết. Không bắt buộc gửi tọa độ GPS chính xác nếu ngữ cảnh POI/Region là đủ.
- Lịch sử hội thoại cá nhân được lưu ở Client; không gửi toàn bộ lịch sử lên Server theo mặc định.
- Backend chỉ ghi số liệu thống kê tổng hợp vào `ChatUsageStats`.
- Việc xử lý câu hỏi có thể cần gửi nội dung tới AI Engine/nhà cung cấp để xử lý, nhưng điều đó không đồng nghĩa với việc lưu toàn bộ hội thoại lâu dài vào database.
- Không đưa nội dung chat đầy đủ vào log mặc định.

### 8.2. Gửi phản hồi Chat AI

`POST /api/v1/chat/feedback`

Request:

```json
{
  "languageCode": "en-US",
  "submittedExcerpt": "Selected conversation excerpt...",
  "feedbackText": "The answer contains an inaccurate historical detail.",
  "feedbackType": "INACCURATE_ANSWER"
}
```

Response `201 Created`:

```json
{
  "feedbackId": 501,
  "createdAt": "2026-11-17T03:10:00Z"
}
```

**Quy tắc:**

- Client phải hiển thị nội dung được chọn để khách xem lại và xác nhận trước khi gửi.
- Chỉ lưu đoạn hội thoại khách chủ động gửi; không tự động gửi toàn bộ lịch sử.
- `submittedExcerpt` có thể để `null` nếu khách chỉ gửi góp ý mà không đính kèm đoạn hội thoại.
- `feedbackType` phải thuộc danh sách giá trị Backend hỗ trợ.

---

## 9. Xác thực Super Admin

### 9.1. Đăng nhập

`POST /api/v1/auth/login`

Request:

```json
{
  "username": "admin",
  "password": "123456"
}
```

Response `200 OK`:

```json
{
  "accessToken": "<JWT_TOKEN>",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

**Quy tắc:**

- Chỉ tài khoản Super Admin duy nhất `admin` được phép đăng nhập.
- Mật khẩu seed ban đầu được lưu dạng BCrypt; cần đổi mật khẩu mặc định sau lần đăng nhập đầu tiên theo chính sách bảo mật triển khai.
- Không có API đăng ký, tạo thêm tài khoản hoặc khôi phục mật khẩu qua email.
- Phiên đăng nhập hết hạn sau 30 phút không có thao tác hợp lệ; Backend phải kiểm tra inactivity, không chỉ dựa vào bộ đếm ở Frontend.
- Sai mật khẩu quá 5 lần liên tiếp thì khóa đăng nhập tạm thời 15 phút.
- Không trả thông tin giúp phân biệt tài khoản có tồn tại hay không.

### 9.2. Đổi mật khẩu

`PATCH /api/v1/auth/change-password`

Header:

```http
Authorization: Bearer <JWT_TOKEN>
```

Request:

```json
{
  "currentPassword": "123456",
  "newPassword": "new-secure-password"
}
```

Response `204 No Content`.

Backend xác minh mật khẩu hiện tại, kiểm tra chính sách mật khẩu mới và lưu BCrypt hash. Không trả mật khẩu hoặc password hash trong response.

---

## 10. Admin API – Quản lý POI, Region, ngôn ngữ và QR

Tất cả endpoint trong mục này yêu cầu:

```http
Authorization: Bearer <JWT_TOKEN>
```

### 10.1. Danh sách và chi tiết POI

- `GET /api/v1/admin/poi`
- `GET /api/v1/admin/poi/{siteId}`

Response POI cần thể hiện các trường nghiệp vụ theo ERD/DataDictionary, bao gồm `categoryId`, `regionId`, tọa độ, `triggerRadius`, `imageUrl`, `referenceTicketPrice`, `status` và các cấu hình ngôn ngữ theo POI nếu màn hình cần.

### 10.2. Tạo POI

`POST /api/v1/admin/poi`

Request:

```json
{
  "categoryId": 2,
  "regionId": 1,
  "name": "Dinh Độc Lập",
  "description": "Di tích lịch sử tại TP. Hồ Chí Minh.",
  "address": "Quận 1, TP. Hồ Chí Minh",
  "latitude": 10.776889,
  "longitude": 106.695333,
  "triggerRadius": 20,
  "referenceTicketPrice": 40000,
  "status": true
}
```

Response `201 Created`:

```json
{
  "siteId": 1,
  "narrationId": 15,
  "createdAt": "2026-11-17T03:15:00Z"
}
```

**Quy tắc:**

- Backend lấy `CreatedBy` từ tài khoản Super Admin đã xác thực, không nhận `createdBy` tùy ý từ Client.
- Việc tạo POI và Narration phải được thực hiện theo transaction để bảo đảm một POI có đúng một Narration.
- `RegionId` và `CategoryId` phải tham chiếu bản ghi hợp lệ.
- Validation tọa độ, bán kính và dữ liệu bắt buộc phải được thực hiện ở Backend.
- Chỉ tăng `DataVersion` nếu dữ liệu được tạo thuộc phạm vi gói POI sync.

### 10.3. Cập nhật và xóa POI

- `PATCH /api/v1/admin/poi/{siteId}`
- `DELETE /api/v1/admin/poi/{siteId}`

Backend kiểm tra ràng buộc QR, Narration và dữ liệu liên quan trước khi xóa. Nếu không thể xóa vật lý an toàn, có thể dùng `status=false` theo chính sách dữ liệu; hành vi xóa mềm/vật lý cần thống nhất khi triển khai.

Chỉ tăng `DataVersion` khi thay đổi làm thay đổi gói POI sync.

### 10.4. Quản lý Region

- `GET /api/v1/admin/regions`
- `POST /api/v1/admin/regions`
- `PATCH /api/v1/admin/regions/{regionId}`
- `DELETE /api/v1/admin/regions/{regionId}`

Region là thực thể địa lý riêng, không thay thế Category. Không cho xóa Region đang được POI tham chiếu nếu chưa xử lý quan hệ đó.

### 10.5. Quản lý ngôn ngữ theo POI

- `GET /api/v1/admin/poi/{siteId}/languages`
- `PUT /api/v1/admin/poi/{siteId}/languages`

Request minh họa:

```json
{
  "languages": [
    {
      "languageCode": "vi-VN",
      "isEnabled": true
    },
    {
      "languageCode": "en-US",
      "isEnabled": true
    },
    {
      "languageCode": "ja-JP",
      "isEnabled": true
    },
    {
      "languageCode": "fr-FR",
      "isEnabled": false
    }
  ]
}
```

Response `200 OK`:

```json
{
  "siteId": 1,
  "languages": [
    {
      "languageCode": "vi-VN",
      "isEnabled": true
    },
    {
      "languageCode": "en-US",
      "isEnabled": true
    },
    {
      "languageCode": "ja-JP",
      "isEnabled": true
    },
    {
      "languageCode": "fr-FR",
      "isEnabled": false
    }
  ]
}
```

**Quy tắc:**

- `SiteLanguages` là cấu hình bật/tắt, không được suy ra chỉ từ việc có Translation.
- Backend kiểm tra locale tồn tại trong `Languages`.
- Các ngôn ngữ nguồn thủ công `vi-VN` và `en-US` phải được xử lý theo chính sách bắt buộc của hệ thống; không để thao tác cấu hình ngôn ngữ bổ sung vô tình xóa nội dung nguồn.
- Chỉ ngôn ngữ được bật và có nội dung đủ điều kiện mới được xem là sẵn sàng phục vụ khách.
- Việc bật/tắt ngôn ngữ chỉ làm tăng `DataVersion` nếu cấu hình đó thực sự nằm trong gói POI sync.

### 10.6. Quản lý QR

- `GET /api/v1/admin/poi/{siteId}/qr-codes`
- `POST /api/v1/admin/poi/{siteId}/qr-codes`
- `PATCH /api/v1/admin/qr-codes/{qrId}`
- `DELETE /api/v1/admin/qr-codes/{qrId}`

Request tạo QR:

```json
{
  "qrCodeValue": "LINGUAFLOW-POI-001",
  "isActive": true
}
```

Response `201 Created`:

```json
{
  "qrId": 1001,
  "siteId": 1,
  "qrCodeValue": "LINGUAFLOW-POI-001",
  "isActive": true
}
```

`qrCodeValue` phải duy nhất. QR chỉ được dùng để tra cứu khi `isActive=true`.

### 10.7. Tải ảnh POI

`POST /api/v1/admin/poi/{siteId}/images`

- `Content-Type: multipart/form-data`.
- Chỉ nhận định dạng ảnh được Backend cho phép.
- Dung lượng tối đa 5MB theo SRS.
- Không cung cấp API upload MP3 cho Admin.
- Response trả về `imageUrl` hoặc reference ảnh đã lưu bền vững.

---

## 11. Admin API – Quản lý Narration, Translation và tác vụ AI

### 11.1. Lấy nội dung Narration theo POI

`GET /api/v1/admin/poi/{siteId}/narration`

Response `200 OK`:

```json
{
  "narrationId": 15,
  "siteId": 1,
  "translations": [
    {
      "translationId": 301,
      "languageCode": "vi-VN",
      "content": "Nội dung tiếng Việt do Admin biên soạn.",
      "sourceType": "MANUAL",
      "reviewStatus": "NOT_REQUIRED",
      "contentStatus": "AVAILABLE",
      "contentVersion": 1
    },
    {
      "translationId": 302,
      "languageCode": "en-US",
      "content": "English narration authored by the Super Admin.",
      "sourceType": "MANUAL",
      "reviewStatus": "NOT_REQUIRED",
      "contentStatus": "AVAILABLE",
      "contentVersion": 1
    }
  ]
}
```

### 11.2. Tạo/cập nhật nội dung thủ công

`PUT /api/v1/admin/poi/{siteId}/narration/translations`

Request:

```json
{
  "translations": [
    {
      "languageCode": "vi-VN",
      "content": "Nội dung thuyết minh tiếng Việt."
    },
    {
      "languageCode": "en-US",
      "content": "English narration content."
    }
  ]
}
```

Response `202 Accepted`:

```json
{
  "narrationId": 15,
  "savedTranslations": [
    {
      "translationId": 301,
      "languageCode": "vi-VN",
      "contentVersion": 2
    },
    {
      "translationId": 302,
      "languageCode": "en-US",
      "contentVersion": 2
    }
  ],
  "audioTasksQueued": true
}
```

**Quy tắc:**

- `vi-VN` và `en-US` do Super Admin biên soạn thủ công.
- Backend tính `ContentHash` và tăng `ContentVersion` khi nội dung thay đổi.
- Khi Translation thay đổi, Audio cũ có hash không còn khớp phải được đánh dấu `STALE`; tạo tác vụ TTS mới theo quy trình.
- TTS chạy nền, không thực hiện đồng bộ trong request lưu nội dung.
- Việc thay đổi nội dung chỉ tăng `DataVersion` nếu nội dung đó thuộc phạm vi dữ liệu thực sự được đóng gói trong POI sync; theo thiết kế đồng bộ hiện tại, gói POI ban đầu không chứa Narration/Audio.

### 11.3. Yêu cầu AI tạo bản dịch quản trị

`POST /api/v1/admin/narrations/{narrationId}/translation-tasks`

Request:

```json
{
  "targetLanguageCodes": ["zh-CN", "ja-JP", "hi-IN", "fr-FR", "ar"]
}
```

Response `202 Accepted`:

```json
{
  "narrationId": 15,
  "sourceLanguageCode": "en-US",
  "tasks": [
    {
      "taskId": 8001,
      "targetLanguageCode": "ja-JP",
      "taskSource": "ADMIN",
      "status": "PENDING"
    },
    {
      "taskId": 8002,
      "targetLanguageCode": "fr-FR",
      "taskSource": "ADMIN",
      "status": "PENDING"
    }
  ]
}
```

**Quy tắc:**

- Backend chỉ nhận ngôn ngữ tồn tại và được bật cho POI.
- Backend lấy `en-US` làm nguồn, tự tính `SourceContentHash`.
- Mỗi ngôn ngữ đích có trạng thái task độc lập.
- Kết quả dịch được lưu `SourceType=AI_ADMIN`, `ReviewStatus=PENDING`.
- Không cho phép Client truyền nội dung nguồn tùy ý thay thế Translation `en-US` hiện hành.

### 11.4. Theo dõi tác vụ dịch

`GET /api/v1/admin/translation-tasks/{taskId}`

Response `200 OK`:

```json
{
  "taskId": 8001,
  "narrationId": 15,
  "languageCode": "ja-JP",
  "taskSource": "ADMIN",
  "status": "COMPLETED",
  "retryCount": 0,
  "errorMessage": null,
  "createdAt": "2026-11-17T03:20:00Z",
  "updatedAt": "2026-11-17T03:20:05Z"
}
```

Danh sách task có thể được lấy bằng `GET /api/v1/admin/narrations/{narrationId}/translation-tasks`.

### 11.5. Duyệt bản dịch AI

- `POST /api/v1/admin/translations/{translationId}/approve`
- `POST /api/v1/admin/translations/{translationId}/reject`

Request reject:

```json
{
  "reason": "Bản dịch chưa chính xác về tên địa danh."
}
```

Response approve `200 OK`:

```json
{
  "translationId": 303,
  "reviewStatus": "APPROVED",
  "reviewedAt": "2026-11-17T03:25:00Z",
  "audioStatus": "PENDING"
}
```

**Quy tắc:**

- Chỉ bản dịch AI quản trị đang chờ duyệt và còn hợp lệ mới được duyệt.
- Backend lấy `ReviewedBy` từ tài khoản Super Admin trong JWT, không nhận từ request body.
- Khi duyệt thành công, Backend tạo/đưa tác vụ TTS vào hàng đợi.
- Từ chối bản dịch không được đưa vào quy trình TTS xuất bản.
- Nếu nội dung nguồn đã đổi và bản dịch trở thành stale, không cho duyệt phiên bản cũ.

### 11.6. Theo dõi trạng thái Audio

`GET /api/v1/admin/translations/{translationId}/audio`

Response `200 OK`:

```json
{
  "audioId": 501,
  "translationId": 303,
  "status": "READY",
  "contentHash": "sha256-content-hash",
  "audioVersion": 1,
  "storageUrl": "https://storage.example/audio/1/ja-JP/hash.mp3",
  "generatedAt": "2026-11-17T03:30:00Z",
  "retryCount": 0,
  "errorMessage": null
}
```

Trạng thái Audio độc lập với `ReviewStatus` và `ContentStatus` của Translation.

---

## 12. Admin API – Dashboard và quản lý AI

### 12.1. Dashboard tổng quan

`GET /api/v1/admin/dashboard/overview?from={date}&to={date}`

Response `200 OK`:

```json
{
  "onlineVisitors": 18,
  "activeGpsSessions": 11,
  "qrScans": 245,
  "audioListens": 680,
  "chatRequests": 120,
  "topSites": [
    {
      "siteId": 1,
      "name": "Dinh Độc Lập",
      "listenCount": 180
    }
  ],
  "generatedAt": "2026-11-17T03:35:00Z"
}
```

**Quy tắc:**

- `onlineVisitors` dựa trên `LastHeartbeatAt`.
- `activeGpsSessions` dựa trên `LastGpsHeartbeatAt`; không đồng nhất với online visitors.
- Ngưỡng thời gian hoạt động lấy từ cấu hình Server.
- Dashboard có thể cung cấp endpoint riêng cho biểu đồ lịch sử, lượt quét QR, Audio listens, Chat usage, POI phổ biến và Chat Feedback.
- Không hiển thị toàn bộ lịch sử hội thoại cá nhân.

### 12.2. Dữ liệu thống kê lịch sử

Các endpoint truy vấn lịch sử có thể tách theo nhóm:

- `GET /api/v1/admin/dashboard/qr-scans?from={date}&to={date}`
- `GET /api/v1/admin/dashboard/audio-listens?from={date}&to={date}`
- `GET /api/v1/admin/dashboard/chat-usage?from={date}&to={date}`
- `GET /api/v1/admin/dashboard/top-sites?from={date}&to={date}`
- `GET /api/v1/admin/chat/feedback?from={date}&to={date}`

Các truy vấn cần phân trang khi trả danh sách dài. Tham số ngày phải được kiểm tra và giới hạn khoảng thời gian hợp lý.

### 12.3. Quản lý cấu hình Chat AI

- `GET /api/v1/admin/ai/configuration`
- `PUT /api/v1/admin/ai/configuration`

Request:

```json
{
  "systemPrompt": "You are a tourism assistant...",
  "bannedKeywords": ["keyword-one", "keyword-two"]
}
```

Response `200 OK`:

```json
{
  "configId": 1,
  "updatedAt": "2026-11-17T03:40:00Z"
}
```

### 12.4. Đồng bộ Knowledge Base

`POST /api/v1/admin/ai/knowledge-base/sync`

Response `202 Accepted`:

```json
{
  "taskId": "kb-sync-20261117-001",
  "status": "PENDING"
}
```

Backend đưa yêu cầu đồng bộ vào xử lý nền. Nội dung POI và Translation đã được duyệt được đưa vào Knowledge Base theo quy tắc RAG. Đồng bộ Knowledge Base không đồng nghĩa với huấn luyện lại mô hình.

---

## 13. Chuẩn hóa lỗi – Problem Details

Các API nên dùng cấu trúc Problem Details nhất quán cho lỗi HTTP. Ví dụ:

**Response 400 Bad Request:**

```json
{
  "type": "https://api.linguaflow.example/problems/validation-error",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed.",
  "instance": "/api/v1/admin/poi",
  "timestamp": "2026-11-17T03:45:00Z",
  "errors": [
    {
      "field": "triggerRadius",
      "message": "Bán kính kích hoạt phải lớn hơn 0."
    }
  ]
}
```

Các trường mở rộng như `timestamp` và `errors` được phép bổ sung; các trường cốt lõi là `type`, `title`, `status`, `detail`, `instance`.

### 13.1. HTTP status code

|                      Status | Ý nghĩa                                      | Trường hợp thường gặp                     |
| --------------------------: | -------------------------------------------- | ----------------------------------------- |
|                    `200 OK` | Thành công                                   | Truy vấn, cập nhật thành công có response |
|               `201 Created` | Tạo mới thành công                           | Tạo POI, Session, Feedback, log           |
|              `202 Accepted` | Đã nhận yêu cầu xử lý nền                    | Dịch AI, TTS, đồng bộ Knowledge Base      |
|            `204 No Content` | Thành công, không có body                    | Heartbeat, kết thúc session, đổi mật khẩu |
|          `304 Not Modified` | Không có thay đổi                            | DataVersion của Client trùng Server       |
|           `400 Bad Request` | Request sai/thiếu dữ liệu                    | Validation                                |
|          `401 Unauthorized` | Chưa xác thực hoặc JWT không hợp lệ          | API Admin                                 |
|             `403 Forbidden` | Đã xác thực nhưng không được phép            | Truy cập chức năng Admin không phù hợp    |
|             `404 Not Found` | Không tìm thấy tài nguyên                    | POI, QR, Translation, Region              |
|              `409 Conflict` | Xung đột trạng thái/dữ liệu                  | Duyệt bản dịch stale, xung đột cập nhật   |
|     `413 Payload Too Large` | Payload vượt giới hạn                        | Ảnh lớn hơn 5MB                           |
|  `422 Unprocessable Entity` | Dữ liệu đúng cấu trúc nhưng không xử lý được | Locale không hỗ trợ                       |
|     `429 Too Many Requests` | Vượt giới hạn request                        | Rate limit hoặc đăng nhập bị giới hạn     |
| `500 Internal Server Error` | Lỗi nội bộ                                   | Lỗi không dự kiến                         |
|           `502 Bad Gateway` | Dịch vụ phụ thuộc trả lỗi                    | AI Provider/Storage upstream              |
|   `503 Service Unavailable` | Dịch vụ tạm thời không khả dụng              | AI/Storage/DB tạm thời mất kết nối        |

Không trả stack trace, mật khẩu, JWT secret, API key hoặc thông tin nội bộ nhạy cảm trong response.

---

## 14. Quy tắc version, cache và tính nhất quán

1. `DataVersion` dùng cho gói POI sync ban đầu; không dùng thay cho phiên bản Translation hoặc Audio.
2. `ContentVersion` và `ContentHash` theo dõi nội dung Translation.
3. `AudioVersion` và `Audio.ContentHash` theo dõi Audio được tạo từ nội dung nào.
4. Khi Translation thay đổi, Audio có hash cũ phải thành `STALE`; Client không được phát Audio stale.
5. Cache POI ở Redis phải gắn với `DataVersion`.
6. Client cache Audio theo ID/version/hash, không dựa duy nhất vào URL.
7. Không tăng `DataVersion` chỉ vì TTS hoàn thành, Audio đổi trạng thái hoặc file được tạo lại.
8. Nếu sau này có gói đồng bộ Translation riêng, cần định nghĩa version/manifest riêng thay vì tự mở rộng ý nghĩa `DataVersion` hiện tại.
9. Các thao tác cập nhật nhiều bảng liên quan (POI/Narration/Translation/Audio metadata) cần transaction hoặc cơ chế đảm bảo nhất quán phù hợp.

---

## 15. Các điểm cần chốt khi triển khai

Những điểm dưới đây là chi tiết thiết kế triển khai, chưa nên coi là quyết định nghiệp vụ đã khóa nếu chưa được thống nhất:

1. Dùng Backend proxy hay URL trực tiếp/URL ký của Object Storage cho Audio.
2. Cơ chế phân trang cụ thể cho danh sách POI, QR, task và Dashboard.
3. Có dùng endpoint riêng để ghi QR scan hay ghi log ngay trong API tra QR.
4. Cách biểu diễn trạng thái tác vụ Knowledge Base và endpoint truy vấn tiến độ.
5. Chính sách xóa POI: xóa mềm hay xóa vật lý khi còn QR/log/Audio liên quan.
6. Danh sách `feedbackType` hợp lệ và giới hạn kích thước `submittedExcerpt`.
7. Rate limit cụ thể cho API Visitor và giới hạn đăng nhập.
8. Cách cấp/refresh URL Audio nếu Storage URL có thời hạn.

---

## 16. Checklist đối chiếu với các tài liệu nền

- [ ] Gói POI sync có Region metadata nhưng không chứa toàn bộ Translation/Audio.
- [ ] Trả 304 khi `clientVersion` trùng `serverDataVersion`.
- [ ] GPS được xử lý ở Client; không cần gửi GPS liên tục lên Backend để Geofencing.
- [ ] QR xác định POI rồi suy ra Region, không cần GPS.
- [ ] Audio Manifest được lọc theo Region và narration language.
- [ ] Chỉ Audio READY, đúng hash và đủ điều kiện xuất bản mới được tải.
- [ ] Có fallback `en-US` rõ ràng, không gắn nhãn nhầm ngôn ngữ Audio.
- [ ] Dịch on-demand thành công được trả ngay; thất bại thì fallback `en-US`.
- [ ] AI_ADMIN cần Super Admin duyệt trước khi xuất bản và TTS.
- [ ] Translation và Audio có trạng thái độc lập.
- [ ] Session website và GPS được thống kê riêng.
- [ ] Chat history không được gửi/lưu toàn bộ trên Server theo mặc định.
- [ ] Chat Feedback chỉ được gửi sau khi khách xem lại và xác nhận.
- [ ] Admin API được bảo vệ bằng JWT và kiểm tra inactivity ở Backend.
- [ ] Các endpoint và DTO tham chiếu đúng ERD(2)/DataDictionary(2).
