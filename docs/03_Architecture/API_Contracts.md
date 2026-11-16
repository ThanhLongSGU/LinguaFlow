# LINGUAFLOW API CONTRACTS

Tài liệu này định nghĩa cấu trúc JSON tiêu chuẩn (API Contract) cho luồng Đồng bộ dữ liệu POI offline và Geofencing.

## 1. Đồng bộ gói POI Offline (GET `/api/v1/sync/poi?clientVersion={v}`)

API này trả về danh sách toàn bộ các địa điểm kèm tọa độ để Client (React/PWA) lưu vào IndexedDB và chạy Geofencing khi mất mạng ví dụ như:

**Response (200 OK):**

```json
{
  "serverDataVersion": 5,
  "isUpdated": true,
  "poiList": [
    {
      "siteId": 1,
      "name": "Dinh Độc Lập",
      "categoryId": 2,
      "description": "Di tích quốc gia đặc biệt...",
      "latitude": 10.776889,
      "longitude": 106.695333,
      "triggerRadius": 20,
      "imageUrl": "/static/images/dinhdoclap.jpg",
      "translations": [
        {
          "languageCode": "vi",
          "audioUrl": "/static/audio/dinhdoclap_vi.mp3"
        },
        {
          "languageCode": "en",
          "audioUrl": "/static/audio/dinhdoclap_en.mp3"
        }
      ]
    }
  ]
}
```

Lưu ý kiến trúc: Nếu clientVersion gửi lên bằng với serverDataVersion trong DB, API chỉ trả về HTTP Status 304 (Not Modified), không cần trả body JSON để tiết kiệm băng thông

## 3. Xác thực Super Admin

### Đăng nhập (POST `/api/v1/auth/login`)

Chỉ tài khoản Super Admin duy nhất `admin` được phép đăng nhập. Mật khẩu mặc định
`123456` được seed và lưu dưới dạng BCrypt; sau đó có thể thay đổi bằng API đổi mật khẩu.

**Request:**

```json
{
  "username": "admin",
  "password": "123456"
}
```

**Response (200 OK):**

```json
{
  "accessToken": "<JWT_TOKEN>",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

### Đổi mật khẩu (PATCH `/api/v1/auth/change-password`)

Yêu cầu header `Authorization: Bearer <JWT_TOKEN>`.

**Request:**

```json
{
  "currentPassword": "123456",
  "newPassword": "new-secure-password"
}
```

**Response (204 No Content):** Mật khẩu mới được kiểm tra và lưu dưới dạng BCrypt.

Không tồn tại endpoint đăng ký, tạo mới tài khoản hoặc khôi phục mật khẩu qua email.

## 2. Chuẩn hóa Định dạng Lỗi (Error Response - RFC 7807)

Mọi lỗi trả về (400, 403, 404, 500) phải bọc trong cấu trúc chuẩn Problem Details ví dụ như sau:
**Response (400 Bad Request):**

```json
{
  "timestamp": "2026-10-19T10:30:00Z",
  "status_code": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/admin/poi",
  "details": [
    {
      "field": "triggerRadius",
      "issue": "Bán kính không được nhỏ hơn 5m"
    }
  ]
}
```
