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
