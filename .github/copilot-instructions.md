# LinguaFlow - System Instructions & Architecture Rules

## 1. Core Architectural Constraints

- **Offline-First:** Tọa độ và thông tin địa điểm (POI) phải được đồng bộ một lần từ backend. Không gọi API liên tục khi người dùng di chuyển.
- **Client-Side Geofencing:** Thuật toán Haversine và logic tính toán bán kính, độ trễ (Hysteresis) phải chạy thuần túy trên Frontend (React).
- **Clean Architecture (Backend):** Tuân thủ nghiêm ngặt mô hình 3 lớp (Controller -> Service -> Repository). Không nhồi nhét logic nghiệp vụ vào Controller.

## 2. Documentation Routing (Tra cứu tài liệu)

Khi cần viết tính năng mới, hãy luôn ưu tiên đọc các file tài liệu sau trước khi sinh code:

- **Yêu câu nghiệp vụ:** Đọc thư mục `docs/01_Requirements/` để nắm các yêu cầu chức năng.
- **Luồng dữ liệu:** Đọc thư mục `docs/02_DataFlow/` để nắm các luồng dữ liệu giữa Frontend và Backend.
- **Kiến trúc hệ thống:** Đọc thư mục `docs/03_Architecture/` để nắm luồng dữ liệu offline.
- **Hợp đồng API:** Đọc thư mục `docs/04_API_Contracts/`. Tuyệt đối không tự bịa ra các field JSON hoặc endpoint không có trong hợp đồng này. Nếu muốn báo thêm field hoặc endpoint mới, hãy dừng lại và báo cho tôi biết.
- **Quy tắc Frontend:** Đọc `docs/UI_UX_Guidelines/` (nếu có) để lấy chuẩn màu sắc và component React.

## 3. Coding Standards

- Sử dụng TypeScript cho toàn bộ Frontend. Luôn định nghĩa Interface/Type rõ ràng.
- Tách biệt logic nghiệp vụ ra khỏi UI Component (Pure Functions).
- Không để lại code thừa (unused imports, variables).
