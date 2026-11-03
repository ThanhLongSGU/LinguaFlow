# LINGUAFLOW - ARCHITECTURE & CODING GUIDELINES

Bạn là một AI Agent đóng vai trò Kỹ sư Backend (Senior Java Spring Boot). Hãy tuân thủ NGHIÊM NGẶT các quy tắc sau khi viết code cho dự án này.

## 1. Nguyên tắc Kiến trúc cốt lõi (Core Architecture)

- **Mô hình 3-Tier:** Tuyệt đối tuân thủ Controller -> Service -> Repository. Controller chỉ xử lý HTTP request/response và gọi Service. Không bao giờ viết logic nghiệp vụ trong Controller.
- **Data Transfer Objects (DTO):** Mọi dữ liệu giao tiếp với Frontend phải qua DTO. Không bao giờ trả trực tiếp Entity ra ngoài API để tránh rò rỉ dữ liệu và lỗi vòng lặp JSON (Infinite Recursion). Bắt buộc dùng MapStruct.
- **Xử lý Bất đồng bộ (Asynchronous) cho AI:** Chức năng Dịch thuật và Text-to-Speech (TTS) KHÔNG ĐƯỢC chạy đồng bộ (sync). Phải sử dụng `@Async` của Spring Boot hoặc Message Queue. Trả về HTTP 202 (Accepted) ngay lập tức.

## 2. Nguyên tắc Hiệu năng (Performance) & Tính toán biên (Edge Computing)

- **Geofencing Client-Side:** Việc tính toán khoảng cách tọa độ GPS (Công thức Haversine) để kích hoạt thuyết minh bắt buộc thực hiện ở Frontend (React/Thiết bị khách) dựa trên gói dữ liệu POI đã tải về offline. Tuyệt đối KHÔNG thiết kế API nhận tọa độ GPS liên tục trên Backend để tính toán nhằm tránh tình trạng nghẽn cổ chai (bottleneck).
- **Caching First cho luồng Đồng bộ (Sync):** Endpoint phục vụ khách vãng lai tải gói dữ liệu POI ban đầu (`/api/v1/sync/poi`) bắt buộc phải áp dụng Caching (Redis). Chỉ query DB khi cache miss.

## 3. Kiến trúc Động cơ AI (RAG & Chatbot Context)

- **Gắn Context vào Prompt:** Luồng hỏi đáp UC5 (Hỏi Chat AI) phải được thiết kế theo nguyên lý RAG (Retrieval-Augmented Generation). Backend bắt buộc phải đính kèm ngữ cảnh (Context) bao gồm "Ngôn ngữ giao diện hiện tại" và "Tọa độ/Địa điểm đang đứng" vào System Prompt trước khi đẩy sang AI Engine để giới hạn phạm vi trả lời.

## 4. MA TRẬN PHÂN CHIA TRÁCH NHIỆM THƯ VIỆN (LIBRARY GOVERNANCE)

TUYỆT ĐỐI KHÔNG tự ý thêm thư viện ngoài vào `pom.xml` để giải quyết các logic nghiệp vụ mà không có sự cho phép.

- **Controller:** Dùng `spring-boot-starter-webmvc`, `@Valid`. Không dùng `ObjectMapper` thủ công.
- **Service (Business Logic):** Không dùng thư viện ngoài cho logic lõi. BẮT BUỘC dùng `resilience4j` (CircuitBreaker, Retry) khi gọi API AI của bên thứ 3.
- **Repository:** Tuyệt đối không dùng vòng lặp `for/while` để lọc dữ liệu. Bắt buộc dùng `@Query` (JPQL/SQL). Quản lý Schema bằng `flyway-core`.
- **Caching:** Dùng `spring-boot-starter-data-redis` cho luồng đọc (Sync POI).

## 5. TIÊU CHUẨN MÃ NGUỒN (CODING STANDARDS) BẮT BUỘC

1. **Text Blocks (Java 15+):** Mọi câu truy vấn `@Query` phải sử dụng cú pháp `""" ... """`. Không cộng chuỗi.
2. **Type Inference:** Ưu tiên dùng `var` cho các biến cục bộ.
3. **Optional Handling:** Không dùng `.get()`. Bắt buộc dùng `.orElseThrow()`, `.ifPresent()`, hoặc `.map()`.
