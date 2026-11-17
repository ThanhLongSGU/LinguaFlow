# LinguaFlow --- Hướng dẫn khởi động hệ thống

Tài liệu này hướng dẫn chạy **Backend Spring Boot** và **Frontend
React + Vite** trên Windows bằng PowerShell.

> Giả định cấu trúc dự án tại thư mục gốc:
>
> ``` text
> LinguaFlow/
> ├── backend/
> ├── frontend/
> └── README.md
> ```
>
> Nếu tên thư mục hoặc cổng chạy trong cấu hình dự án khác với tài liệu
> này, hãy ưu tiên cấu hình thực tế trong repository.

------------------------------------------------------------------------

## 1. Chuẩn bị môi trường

Mở PowerShell tại thư mục gốc `LinguaFlow`.

Kiểm tra các công cụ:

``` powershell
java -version
.\backend\mvnw.cmd -version
node -v
pnpm -v
```

Các công cụ cần có:

-   **JDK** tương thích với phiên bản Java được khai báo trong
    `backend/pom.xml`.
-   **Node.js** tương thích với cấu hình frontend.
-   **pnpm** để cài đặt dependency frontend.
-   **SQL Server** nếu backend đang sử dụng SQL Server làm cơ sở dữ
    liệu.

Nếu lệnh `pnpm` hoặc `node` không được nhận diện, kiểm tra cấu hình
PATH/FNM trước khi tiếp tục.

## 2. Cấu hình cơ sở dữ liệu

Trước khi chạy backend:

1.  Khởi động dịch vụ SQL Server trên máy.
2.  Kiểm tra tên máy chủ/instance, tên database, tài khoản và mật khẩu.
3.  Kiểm tra cấu hình kết nối trong
    `backend/src/main/resources/application.properties` hoặc
    `application.yml` (và các file profile tương ứng).
4.  Đảm bảo database đã được tạo, hoặc ứng dụng có cơ chế tự tạo
    database/schema phù hợp.

Không đưa mật khẩu database, JWT secret hoặc thông tin nhạy cảm vào Git.
Nếu dự án sử dụng biến môi trường hoặc file `.env`, hãy cấu hình theo
hướng dẫn hiện có của dự án.

## 3. Cài đặt dependency

Chỉ cần thực hiện khi mới clone dự án, khi dependency thay đổi, hoặc khi
thư mục cài đặt dependency bị thiếu.

### Backend --- Maven

Tại thư mục gốc:

``` powershell
cd backend
.\mvnw.cmd -version
```

Maven Wrapper sẽ tự tải Maven phù hợp nếu máy chưa có phiên bản được yêu
cầu.

### Frontend --- pnpm

Mở terminal PowerShell thứ hai hoặc quay lại thư mục gốc:

``` powershell
cd frontend
pnpm install
```

Chờ lệnh hoàn tất. Nếu registry phản hồi chậm, xem tiến trình tải và log
trước khi hủy; không chạy nhiều lệnh cài đặt đồng thời trong cùng thư
mục.

## 4. Khởi động Backend Spring Boot

Mở **Terminal 1** tại thư mục gốc dự án:

``` powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Nếu backend chạy thành công, terminal sẽ hiển thị log Spring Boot và
thông báo ứng dụng đã khởi động.

Địa chỉ backend mặc định thường là:

``` text
http://localhost:8080
```

Đây chỉ là cổng mặc định thường dùng của Spring Boot. Hãy kiểm tra
`server.port` trong cấu hình nếu dự án đặt cổng khác.

**Giữ Terminal 1 mở** trong suốt thời gian sử dụng backend. Nhấn
`Ctrl + C` để dừng backend.

## 5. Khởi động Frontend Vite

Mở **Terminal 2** tại thư mục gốc dự án:

``` powershell
cd frontend
pnpm dev
```

Vite sẽ in địa chỉ Local trong terminal, thường là:

``` text
http://localhost:5173
```

Mở đúng URL được Vite hiển thị trong trình duyệt.

**Giữ Terminal 2 mở** trong suốt thời gian sử dụng frontend. Nhấn
`Ctrl + C` để dừng Vite.

## 6. Thứ tự khởi động hằng ngày

Sau khi đã cài dependency và cấu hình môi trường, mỗi lần làm việc chỉ
cần:

1.  Khởi động SQL Server.

2.  Mở Terminal 1 và chạy Backend:

    ``` powershell
    cd backend
    .\mvnw.cmd spring-boot:run
    ```

3.  Mở Terminal 2 và chạy Frontend:

    ``` powershell
    cd frontend
    pnpm dev
    ```

4.  Mở URL Local mà Vite cung cấp.

Không cần chạy lại `pnpm install` mỗi lần khởi động.

## 7. Kiểm tra khi ứng dụng không chạy

### Backend không khởi động

-   Kiểm tra `java -version`.
-   Kiểm tra log lỗi đầu tiên trong Terminal 1.
-   Kiểm tra SQL Server có đang chạy không.
-   Kiểm tra JDBC URL, database, username/password và cấu hình profile.
-   Kiểm tra cổng backend có bị ứng dụng khác chiếm không.

### Frontend không khởi động

-   Kiểm tra `node -v` và `pnpm -v`.

-   Đảm bảo đang đứng trong thư mục `frontend`.

-   Nếu dependency chưa được cài hoặc vừa thay đổi, chạy:

    ``` powershell
    pnpm install
    ```

-   Nếu báo cổng Vite đang được sử dụng, đọc URL/cổng thay thế mà Vite
    hiển thị hoặc dừng tiến trình đang chiếm cổng.

### Frontend gọi API nhưng bị lỗi

-   Đảm bảo backend đã khởi động thành công.
-   Kiểm tra URL API trong cấu hình frontend (ví dụ file `.env` hoặc
    biến `VITE_*`).
-   Kiểm tra cấu hình CORS phía backend.
-   Mở Developer Tools của trình duyệt → Network/Console để xem request
    và lỗi cụ thể.

## 8. Lệnh tắt nhanh

  Công việc                 Lệnh
  ------------------------- ------------------------------------------
  Chạy Backend              `cd backend; .\mvnw.cmd spring-boot:run`
  Chạy Frontend             `cd frontend; pnpm dev`
  Cài dependency Frontend   `cd frontend; pnpm install`
  Dừng tiến trình           `Ctrl + C` trong terminal tương ứng

------------------------------------------------------------------------

## Ghi chú dự án

Các giá trị sau cần đối chiếu với cấu hình thực tế của LinguaFlow, không
mặc định rằng mọi máy đều giống nhau:

-   Phiên bản Java trong `backend/pom.xml`.
-   `server.port` và Spring profile đang dùng.
-   SQL Server instance, database và thông tin xác thực.
-   URL API mà frontend sử dụng.
-   Port thực tế Vite in ra khi chạy `pnpm dev`.

**Mẹo:** Có thể lưu tài liệu này tại thư mục gốc với tên
`DEVELOPMENT.md` để cả nhóm cùng sử dụng.
