HƯỚNG DẪN CÀI ĐẶT & CHUẨN LẬP TRÌNH DỰ ÁN LINGUAFLOW
1. Cài Đặt Môi Trường
Dự án sử dụng Spring Boot 4.1.1 và Java 25. Để tối ưu hóa tài nguyên phần cứng, nhóm cần thống nhất các công cụ sau:
• JDK: Tải và cài đặt JDK 25 (Amazon Corretto hoặc Eclipse Temurin).
• IDE: Khuyến nghị dùng IntelliJ IDEA Community. Nếu máy tính có RAM hạn chế, nên chuyển sang Visual Studio Code kết hợp Extension Pack for Java để nhẹ máy hơn.
• Database: Cài đặt SQL Server và SQL Server Management Studio (SSMS).
• Quản lý gói: Cài đặt Maven (hoặc dùng lệnh mvnw có sẵn trong source code).
2. Kiến Trúc 3 Lớp & Quy Định Lập Trình
Tuyệt đối không viết code tắt. Mọi tính năng phải tuân thủ luồng: Controller -> Service -> Repository -> Database.
• Tầng Controller: Chỉ nhận HTTP Request, gọi Service và trả về DTO. Tuyệt đối KHÔNG trả Entity ra ngoài để tránh rò rỉ bảo mật.
• Tầng Service: Nơi chứa logic nghiệp vụ cốt lõi. Sử dụng @RequiredArgsConstructor của Lombok để tiêm (inject) các Dependency.
• Mapper: Bắt buộc dùng MapStruct để ánh xạ dữ liệu giữa Entity và DTO. Tuyệt đối không map bằng tay (get/set thủ công).
• Tầng Repository: Khai báo các hàm thao tác CSDL bằng JpaRepository.
• Ngoại lệ (Exception): Nếu có lỗi, ném ra các Exception chuẩn (như EntityNotFoundException). GlobalExceptionHandler sẽ tự động bắt và trả về JSON chuẩn hóa.
3. Quy Chuẩn Quản Lý Mã Nguồn (Git)
Quy trình làm việc:
1. Tuyệt đối không push code trực tiếp lên nhánh main.
2. Tạo nhánh riêng cho từng tính năng: git checkout -b feature/ten-tinh-nang
3. Hoàn thành thì tạo Pull Request để đồng đội Code Review.

Cú pháp Commit bắt buộc (Conventional Commits): <loại>: <mô tả ngắn gọn>
• feat: Thêm tính năng mới (Vd: feat: Thêm API tạo danh mục)
• fix: Sửa lỗi hệ thống (Vd: fix: Sửa lỗi crash khi quét QR sai)
• refactor: Tái cấu trúc mã nguồn, tối ưu hóa (Vd: refactor: Đổi int sang Object trong Entity)
• chore: Các cấu hình, thư viện (Vd: chore: Cập nhật thư viện MapStruct)
