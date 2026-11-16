# Tài liệu Đặc tả Yêu cầu Sản phẩm (PRD) - Hệ thống LinguaFlow

## 1. Quy tắc Nghiệp vụ (Business Rules)

- **BR-01:** Dữ liệu POI (tọa độ, bán kính, thông tin cơ bản) bắt buộc phải được đồng bộ và lưu trữ cục bộ (Offline-first) trên thiết bị của du khách ngay trong lần truy cập đầu tiên.
- **BR-02:** Bản đồ phải hiển thị trực quan vị trí hiện tại của người dùng và bán kính vùng kích hoạt (Geofence) của từng địa điểm.
- **BR-03:** Thuật toán định vị (Haversine) và lọc nhiễu GPS (bỏ qua tín hiệu sai số > 150m, hysteresis 40m) phải được tính toán hoàn toàn phía Client để tiết kiệm tài nguyên máy chủ.
- **BR-04:** Hệ thống phải hỗ trợ chuyển đổi đa ngôn ngữ cho cả giao diện (UI) và nội dung thuyết minh (Audio/TTS). Câu lệnh gửi cho AI Chatbot bắt buộc ép AI trả lời bằng ngôn ngữ du khách đang chọn.
- **BR-05:** Cơ chế phát tự động theo GPS phải kiểm tra hàng chờ âm thanh để tránh phát chồng chéo nhiều địa điểm cùng lúc.
- **BR-06:** Quét mã QR hoặc chọn trực tiếp địa điểm trên bản đồ có độ ưu tiên cao hơn GPS, cho phép phát thuyết minh ngay lập tức.
- **BR-07:** Kích hoạt cơ chế Cooldown (5 phút) cho mỗi địa điểm đã phát tự động qua GPS để tránh làm phiền khi du khách đi ra/vào ranh giới liên tục.
- **BR-08:** Giới hạn dung lượng tệp tải lên từ Admin: Tệp âm thanh (Audio) tối đa 20MB, tệp hình ảnh tối đa 5MB.
- **BR-09:** Phiên đăng nhập (Session/JWT) của Quản trị viên tự động hết hạn sau 30 phút không có thao tác.
- **BR-10:** Khóa tạm thời chức năng đăng nhập trong 15 phút nếu nhập sai mật khẩu quá 5 lần liên tiếp.
- **BR-11:** Hệ thống quản trị được thiết kế đóng, chỉ tồn tại duy nhất **01 tài khoản Super Admin**. Tên đăng nhập được khóa cứng là `admin`, mật khẩu khởi tạo mặc định là `123456` (bắt buộc hỗ trợ tính năng đổi mật khẩu). Dữ liệu tài khoản này phải được tự động khởi tạo (Database Seeding) khi hệ thống chạy lần đầu; tuyệt đối không xây dựng API đăng ký tài khoản mới hoặc quên mật khẩu qua email.

---

## 2. Đặc tả Chi tiết Các Use Case (Use Case Specifications)

### UC01: Truy cập và Đồng bộ dữ liệu

- **Actor(s):** Du khách
- **Maturity:** Focused
- **Summary:** Du khách truy cập vào hệ thống. Hệ thống tiến hành tải giao diện và âm thầm đồng bộ hóa danh sách các địa điểm tham quan (tọa độ, bán kính quét, thông tin cơ bản) về thiết bị của người dùng để chuẩn bị cho việc định vị và phát thuyết minh sau đó.
- **Triggers:** Du khách muốn bắt đầu sử dụng phần mềm để nghe thuyết minh du lịch.
- **Preconditions:** Thiết bị phải có kết nối mạng ổn định trong lần khởi chạy đầu tiên.
- **Post Conditions:** Hệ thống đã nắm giữ toàn bộ tọa độ và bán kính của các POI, sẵn sàng cho việc kích hoạt "Thuyết minh tự động theo GPS" (UC04) ngay cả khi mạng bị chập chờn sau đó.
- **Assumptions:** Trình duyệt của du khách hỗ trợ tính năng lưu trữ dữ liệu cục bộ (IndexedDB/LocalStorage).
- **Reference Business Rules:** BR-01

#### Basic Course of Events

1. **Actor:** Du khách quét QR để truy cập vào hệ thống Hướng dẫn Du lịch TP.HCM.
2. **System:** Tải giao diện người dùng cơ bản.
3. **System:** Kiểm tra dữ liệu lưu trữ trên thiết bị (Xem luồng A1).
4. **System:** Nhận thấy đây là lần truy cập đầu tiên (chưa có dữ liệu), kết nối với máy chủ để tải toàn bộ danh sách địa điểm (POI) và các cấu hình ưu tiên (Xem luồng E1, E2).
5. **System:** Lưu trữ gói dữ liệu này vào bộ nhớ của thiết bị để phục vụ việc định vị vị trí.
6. **System:** Hiển thị bản đồ tổng quan đã đánh dấu các địa điểm. Use Case kết thúc thành công.

#### Alternative Paths

- **A1. Đã có dữ liệu từ lần truy cập trước (Truy cập lần 2 trở đi):**
  1. Tại bước 3 của Luồng cơ bản, hệ thống phát hiện thiết bị đã có sẵn dữ liệu địa điểm.
  2. Hệ thống gửi yêu cầu kiểm tra phiên bản dữ liệu (`clientVersion`) lên máy chủ.
  3. Nếu máy chủ phản hồi không có cập nhật mới (HTTP 304 Not Modified), hệ thống bỏ qua bước 4 & 5, sử dụng lại dữ liệu cũ và tiến thẳng đến bước 6. Nếu có bản cập nhật (HTTP 200 OK), hệ thống quay lại chạy bước 4.

#### Exception Paths

- **E1. Không có kết nối mạng (Lần truy cập đầu):** Tại bước 4, nếu thiết bị mất mạng, hệ thống hiển thị thông báo: _"Cần có kết nối mạng để tải dữ liệu ban đầu"_. Use Case tạm dừng cho đến khi có mạng.
- **E2. Máy chủ không phản hồi:** Tại bước 4, nếu máy chủ gặp sự cố, hệ thống hiển thị thông báo: _"Hệ thống đang bảo trì, vui lòng quay lại sau"_. Use Case kết thúc thất bại.

---

### UC02: Xem bản đồ và vị trí

- **Actor(s):** Du khách
- **Maturity:** Focused
- **Summary:** Du khách tương tác với bản đồ số trên hệ thống để quan sát các địa điểm du lịch, định vị vị trí hiện tại của bản thân và xem thông tin chi tiết của một địa điểm cụ thể.
- **Triggers:** Du khách cần tra cứu đường đi, muốn biết các điểm du lịch lân cận hoặc cần đọc thông tin của một địa điểm cụ thể.
- **Preconditions:** Hệ thống đã hoàn tất UC01 (Đồng bộ dữ liệu POI vào thiết bị).
- **Post Conditions:** Hệ thống hiển thị chính xác vùng bản đồ, vị trí người dùng (nếu có) hoặc thông tin chi tiết của địa điểm được chọn.
- **Assumptions:** Dịch vụ bản đồ (OpenStreetMap/Leaflet) hoạt động bình thường và thiết bị có hỗ trợ định vị GPS.
- **Reference Business Rules:** BR-02, BR-03

#### Basic Course of Events

1. **Actor:** Du khách mở hệ thống và chọn chức năng xem bản đồ (hoặc hệ thống tự động hiển thị bản đồ ngay khi mở ứng dụng).
2. **System:** Tải dữ liệu bản đồ và hiển thị các điểm đánh dấu (marker) cùng vòng bán kính của các địa điểm tham quan tại TP.HCM (Xem luồng E1).
3. **System:** Cho phép du khách thao tác vuốt, cuộn, phóng to, thu nhỏ để xem bản đồ tự do.

#### Alternative Paths

- **A1. Xem vị trí hiện tại:**
  1. Du khách nhấn vào biểu tượng _"Vị trí của tôi"_ (My Location) trên bản đồ.
  2. Hệ thống gửi yêu cầu lấy tọa độ GPS của thiết bị (Xem luồng E2).
  3. Hệ thống định vị thành công, cuộn bản đồ về tọa độ hiện tại và hiển thị biểu tượng _"Bạn đang ở đây"_. Quay lại bước 3 của Luồng cơ bản.
- **A2. Xem thông tin chi tiết địa điểm tham quan:**
  1. Du khách nhấn vào một điểm đánh dấu (marker) bất kỳ trên bản đồ.
  2. Hệ thống truy xuất dữ liệu cục bộ và hiển thị bảng thông tin chi tiết của địa điểm đó (tên, hình ảnh, mô tả lịch sử, khoảng cách) (Xem luồng E3).

#### Exception Paths

- **E1. Không tải được dữ liệu bản đồ:** Tại bước 2, nếu mất kết nối mạng hoặc dịch vụ bản đồ lỗi, hệ thống hiển thị khung bản đồ trống kèm thông báo: _"Không thể tải bản đồ, vui lòng kiểm tra kết nối mạng"_.
- **E2. Du khách từ chối cấp quyền định vị GPS:** Tại bước 2 của luồng A1, nếu trình duyệt từ chối quyền truy cập vị trí, hệ thống hiển thị thông báo: _"Vui lòng cấp quyền vị trí để sử dụng tính năng này"_ và quay lại bước 3 của Luồng cơ bản.
- **E3. Dữ liệu địa điểm bị lỗi hoặc trống:** Tại bước 2 của luồng A2, nếu dữ liệu cục bộ thiếu thông tin, hệ thống hiển thị thông báo: _"Dữ liệu đang cập nhật"_.

---

### UC03: Chọn ngôn ngữ

- **Actor(s):** Du khách
- **Maturity:** Focused
- **Summary:** Du khách thay đổi ngôn ngữ hiển thị của hệ thống. Việc thay đổi này cập nhật toàn bộ giao diện và chuyển đổi nguồn dữ liệu audio thuyết minh sang ngôn ngữ tương ứng.
- **Triggers:** Du khách muốn chuyển sang một ngôn ngữ khác để sử dụng ứng dụng.
- **Preconditions:** Hệ thống đã hoàn tất UC01.
- **Post Conditions:** Giao diện và luồng âm thanh thuyết minh tiếp theo sẽ được phát theo ngôn ngữ vừa chọn.
- **Assumptions:** Dữ liệu dịch thuật UI và các file audio thuyết minh đa ngôn ngữ đã được Admin cấu hình sẵn trên máy chủ.
- **Reference Business Rules:** BR-04

#### Basic Course of Events

1. **Actor:** Du khách nhấn vào biểu tượng/nút _"Chọn ngôn ngữ"_ trên giao diện.
2. **System:** Hiển thị danh sách các ngôn ngữ được hỗ trợ (Tiếng Việt, Tiếng Anh...).
3. **Actor:** Du khách bấm chọn một ngôn ngữ mới (Xem luồng A1).
4. **System:** Ghi nhận lựa chọn, cập nhật toàn bộ văn bản trên giao diện và đổi nguồn script/audio thuyết minh sang ngôn ngữ mới (Xem luồng E1).
5. **System:** Hiển thị hoàn tất giao diện với ngôn ngữ mới. Use Case kết thúc thành công.

#### Alternative Paths

- **A1. Hủy chọn ngôn ngữ:** Tại bước 3, du khách bấm ra ngoài hoặc nhấn nút Đóng. Hệ thống đóng danh sách ngôn ngữ, giữ nguyên trạng thái cũ.

#### Exception Paths

- **E1. Mất kết nối khi tải tài nguyên ngôn ngữ:** Tại bước 4, nếu mất mạng khi kéo gói ngôn ngữ/audio mới, hệ thống thông báo lỗi kết nối và giữ nguyên ngôn ngữ đã lưu trong bộ nhớ đệm.

---

### UC04: Nghe thuyết minh (GPS & QR Code)

- **Actor(s):** Du khách
- **Maturity:** Focused
- **Summary:** Hệ thống tự động phát nội dung thuyết minh dựa trên tọa độ GPS (Geofence) khi du khách di chuyển. Du khách cũng có thể chủ động kích hoạt phát âm thanh bằng cách quét mã QR hoặc chọn trực tiếp trên bản đồ.
- **Triggers:** Du khách đến gần một địa điểm du lịch hoặc đứng tại điểm đặt mã QR.
- **Preconditions:** Đã tải dữ liệu POI cục bộ (UC01).
- **Post Conditions:** Hệ thống ghi nhận trạng thái đã phát cho POI vào bộ nhớ cục bộ để quản lý hàng chờ và thời gian Cooldown.
- **Assumptions:** Dịch vụ định vị trên thiết bị được cấp quyền hoạt động.
- **Reference Business Rules:** BR-03, BR-04, BR-05, BR-06, BR-07

#### Basic Course of Events

1. **Actor:** Du khách bật chế độ _"Thuyết minh tự động"_ và di chuyển.
2. **System:** Liên tục theo dõi tọa độ GPS của người dùng (Xem luồng E1).
3. **System:** Đối chiếu tọa độ với dữ liệu POI cục bộ; nhận diện du khách đã bước vào vùng bán kính kích hoạt của một địa điểm (Xem luồng E2).
4. **System:** Kiểm tra hàng chờ âm thanh và trạng thái Cooldown để chống phát trùng lặp.
5. **System:** Tải/lấy gói dữ liệu Audio tương ứng với ngôn ngữ hiện tại, tự động phát âm thanh và hiển thị bảng điều khiển (Play/Pause/Replay). Use Case kết thúc chu trình.

#### Alternative Paths

- **A1. Kích hoạt bằng mã QR (Bỏ qua GPS):**
  1. Du khách dùng camera quét mã QR đặt tại điểm tham quan.
  2. Hệ thống nhận diện ID địa điểm từ mã QR, bỏ qua bước 2 và 3 của Luồng cơ bản, nhảy thẳng tới bước 5 để phát Audio ngay lập tức.
- **A2. Chọn nghe địa điểm tùy ý:**
  1. Du khách nhấn vào một điểm (marker) trên bản đồ và chọn _"Nghe thuyết minh"_.
  2. Hệ thống bỏ qua bước quét tọa độ, nhảy thẳng tới bước 5 để phát Audio.
- **A3. Phát lại:** Du khách nhấn nút _"Phát lại"_ trên bảng điều khiển media; hệ thống tua file Audio về `0:00` và phát lại từ đầu.

#### Exception Paths

- **E1. Mất tín hiệu GPS:** Tại bước 2, nếu không lấy được tọa độ, hệ thống hiển thị trạng thái _"Đang tìm GPS"_ và tạm ngưng quét Geofence. Tính năng quét QR (A1) vẫn hoạt động bình thường.
- **E2. Chặn spam (Cooldown):** Tại bước 3, nếu du khách liên tục đi ra/vào ranh giới của một địa điểm trong thời gian dưới 5 phút, hệ thống từ chối phát lại Audio của điểm đó.

---

### UC05: Hỏi chat AI

- **Actor(s):** Du khách
- **Maturity:** Focused
- **Summary:** Du khách tương tác với trợ lý ảo AI để hỏi đáp các thông tin chuyên sâu về du lịch, lịch sử, văn hóa hoặc xin gợi ý lịch trình bằng ngôn ngữ đã chọn.
- **Triggers:** Du khách cần tìm hiểu thêm thông tin ngoài lề mà bản thuyết minh âm thanh chưa đề cập.
- **Preconditions:** Đã cấu hình ngôn ngữ hiển thị (UC03) và thiết bị có kết nối Internet.
- **Post Conditions:** Hệ thống cập nhật lịch sử hội thoại vào bộ nhớ cục bộ của thiết bị.
- **Assumptions:** Máy chủ AI Engine hoạt động ổn định.
- **Reference Business Rules:** BR-04

#### Basic Course of Events

1. **Actor:** Du khách mở giao diện Trợ lý AI và nhập câu hỏi (hoặc chọn câu hỏi gợi ý).
2. **System:** Kiểm tra kết nối mạng Internet (Xem luồng E1).
3. **System:** Đóng gói câu hỏi kèm ngữ cảnh hiện tại (ngôn ngữ đang chọn, tọa độ vị trí) gửi lên AI Engine xử lý.
4. **System:** AI phân tích và trả về câu trả lời (Xem luồng E2).
5. **System:** Hiển thị câu trả lời lên màn hình chat và lưu đoạn hội thoại vào bộ nhớ đệm cục bộ. Use Case kết thúc chu trình.

#### Alternative Paths

- **A1. Xem lịch sử chat:** Du khách cuộn lên trên hoặc mở mục lịch sử; hệ thống truy xuất bộ nhớ cục bộ và hiển thị các đoạn hội thoại trước đó.

#### Exception Paths

- **E1. Mất kết nối mạng Internet:** Tại bước 2, nếu thiết bị mất mạng, hệ thống chặn thao tác gửi và thông báo: _"Vui lòng kết nối Internet để sử dụng Trợ lý AI"_.
- **E2. Vi phạm chính sách nội dung:** Tại bước 4, nếu câu hỏi chứa từ khóa cấm hoặc nằm ngoài phạm vi du lịch, AI từ chối và phản hồi: _"Xin lỗi, tôi chỉ có thể hỗ trợ các thông tin liên quan đến trải nghiệm du lịch TP.HCM"_.

---

### UC06: Đăng nhập và Quản lý Tài khoản

- **Actor(s):** Super Admin
- **Maturity:** Focused
- **Summary:** Super Admin duy nhất tiến hành xác thực danh tính để truy cập vào hệ thống Quản lý nội dung (CMS), đồng thời có thể thay đổi mật khẩu mặc định để bảo mật hệ thống.
- **Triggers:** Super Admin cần truy cập hệ thống quản trị hoặc thay đổi mật khẩu bảo mật.
- **Preconditions:** Tài khoản `admin` đã được khởi tạo tự động trong cơ sở dữ liệu (Database Seeding).
- **Post Conditions:** Token xác thực (JWT) được cấp phát; Super Admin có toàn quyền truy cập các chức năng quản lý.
- **Assumptions:** Hệ thống vận hành theo mô hình đóng, không tồn tại chức năng tạo mới tài khoản.
- **Reference Business Rules:** BR-09, BR-10, BR-11

#### Basic Course of Events

1. **Actor:** Super Admin truy cập vào đường dẫn trang quản trị (Admin Portal).
2. **System:** Hiển thị biểu mẫu đăng nhập.
3. **Actor:** Nhập Tên đăng nhập (`admin`) và Mật khẩu (mặc định `123456` hoặc mật khẩu đã đổi), sau đó nhấn _"Đăng nhập"_.
4. **System:** Kiểm tra thông tin định danh, đối chiếu mã băm mật khẩu (BCrypt) trong cơ sở dữ liệu (Xem luồng E1, E2).
5. **System:** Xác thực thành công, cấp phát chuỗi định danh JWT có thời hạn hiệu lực.
6. **System:** Chuyển hướng Super Admin vào màn hình Thống kê (Dashboard). Use Case kết thúc thành công.

#### Alternative Paths

- **A1. Đổi mật khẩu Super Admin:**
  1. Sau khi đăng nhập thành công, Super Admin truy cập mục _"Đổi mật khẩu"_.
  2. Nhập mật khẩu hiện tại, mật khẩu mới và xác nhận mật khẩu mới.
  3. Hệ thống kiểm tra mật khẩu hiện tại; nếu chính xác, tiến hành băm mật khẩu mới bằng thuật toán BCrypt và cập nhật vào cơ sở dữ liệu.

#### Exception Paths

- **E1. Sai thông tin đăng nhập:** Tại bước 4, nếu tên đăng nhập hoặc mật khẩu không khớp, hệ thống tăng bộ đếm số lần sai và thông báo: _"Tên đăng nhập hoặc mật khẩu không chính xác"_.
- **E2. Tài khoản bị khóa tạm thời:** Tại bước 4, nếu nhập sai quá 5 lần liên tiếp, hệ thống khóa quyền đăng nhập và cảnh báo: _"Tài khoản tạm khóa do nhập sai quá nhiều lần. Vui lòng thử lại sau 15 phút"_.

---

### UC07: Xem thống kê (Dashboard)

- **Actor(s):** Super Admin
- **Maturity:** Focused
- **Summary:** Hệ thống tổng hợp và hiển thị các số liệu hoạt động (lượt nghe audio, lượt quét QR, thống kê chat AI, địa điểm được quan tâm nhất) theo thời gian thực và lịch sử để Super Admin theo dõi hiệu suất.
- **Triggers:** Super Admin muốn theo dõi hiệu suất vận hành của hệ thống.
- **Preconditions:** Super Admin đã đăng nhập thành công (UC06).
- **Post Conditions:** Không có sự thay đổi nào về mặt dữ liệu gốc.
- **Assumptions:** Hệ thống ghi nhận đầy đủ log tương tác từ phía du khách.
- **Reference Business Rules:** BR-09, BR-11

#### Basic Course of Events

1. **Actor:** Super Admin truy cập vào menu _"Dashboard"_ (hoặc được chuyển hướng tự động sau khi đăng nhập).
2. **System:** Kiểm tra tính hợp lệ của JWT Token (Xem luồng E1).
3. **System:** Truy vấn dữ liệu thời gian thực và dữ liệu lịch sử mặc định (biểu đồ 7 ngày gần nhất) (Xem luồng E2).
4. **System:** Hiển thị các thẻ số liệu tổng quan và biểu đồ phân tích lên giao diện.

#### Alternative Paths

- **A1. Lọc dữ liệu theo mốc thời gian:** Tại bước 4, Super Admin chọn khoảng thời gian tùy chỉnh và nhấn _"Lọc"_. Hệ thống truy vấn lại dữ liệu theo tham số mới và cập nhật biểu đồ.

#### Exception Paths

- **E1. Phiên làm việc hết hạn:** Tại bước 2, nếu Token hết hạn (quá 30 phút không thao tác), hệ thống từ chối truy cập và điều hướng về trang Đăng nhập (UC06).
- **E2. Lỗi truy xuất hoặc dữ liệu rỗng:** Tại bước 3, nếu chưa có dữ liệu thống kê, hệ thống hiển thị biểu đồ trống kèm thông báo: _"Chưa có dữ liệu thống kê trong khoảng thời gian này"_.

---

### UC08: Quản lý nội dung POI (Point of Interest)

- **Actor(s):** Super Admin
- **Maturity:** Focused
- **Summary:** Super Admin thực hiện thêm mới, cập nhật hoặc xóa thông tin các địa điểm tham quan (tọa độ GPS, bán kính, hình ảnh, mã QR và nội dung thuyết minh đa ngôn ngữ).
- **Triggers:** Có địa điểm tham quan mới hoặc cần cập nhật nội dung thuyết minh.
- **Preconditions:** Super Admin đã đăng nhập thành công (UC06).
- **Post Conditions:** Hệ thống tự động tăng phiên bản dữ liệu toàn cục (`DataVersion`) để các thiết bị của du khách đồng bộ bản mới ở lần truy cập kế tiếp (UC01).
- **Assumptions:** Các tệp âm thanh và hình ảnh chuẩn bị tải lên đáp ứng đúng định dạng cho phép.
- **Reference Business Rules:** BR-08, BR-09, BR-11

#### Basic Course of Events

1. **Actor:** Super Admin truy cập vào menu _"Quản lý POI"_.
2. **System:** Kiểm tra tính hợp lệ của phiên đăng nhập, sau đó hiển thị danh sách các POI hiện có (Xem luồng E1).
3. **Actor:** Nhấn _"Thêm mới"_ (hoặc chọn một POI để chỉnh sửa), nhập tọa độ (Lat/Lng), bán kính kích hoạt, tên, mô tả và tải lên tệp Audio/Hình ảnh.
4. **System:** Kiểm tra tính hợp lệ của dữ liệu và dung lượng tệp tải lên (Xem luồng E2).
5. **Actor:** Nhấn nút _"Lưu"_.
6. **System:** Lưu thông tin vào cơ sở dữ liệu, kích hoạt trigger tăng phiên bản dữ liệu đồng bộ, và cập nhật lại danh sách POI. Use Case kết thúc chu trình.

#### Alternative Paths

- **A1. Xóa địa điểm (POI):**
  1. Tại danh sách POI, Super Admin chọn một địa điểm và nhấn _"Xóa"_.
  2. Hệ thống kiểm tra ràng buộc (chỉ cho phép xóa nếu địa điểm không còn dính dáng đến mã QR hoặc bài thuyết minh đang hoạt động) và hiển thị hộp thoại xác nhận.
  3. Super Admin xác nhận; hệ thống xóa dữ liệu và tăng phiên bản đồng bộ.

#### Exception Paths

- **E1. Phiên làm việc hết hạn:** Tại bước 2, nếu Token hết hạn, hệ thống đẩy người dùng về trang Đăng nhập (UC06).
- **E2. Vi phạm dung lượng tệp:** Tại bước 4, nếu tệp Audio vượt quá 20MB hoặc Ảnh vượt quá 5MB, hệ thống chặn thao tác và thông báo: _"File tải lên vượt quá dung lượng cho phép"_.

---

### UC09: Quản lý hệ thống AI

- **Actor(s):** Super Admin
- **Maturity:** Focused
- **Summary:** Super Admin cấu hình chỉ dẫn hệ thống (System Prompt), quản lý danh sách từ khóa cấm và kích hoạt đồng bộ dữ liệu địa điểm (POI) vào cơ sở tri thức (Knowledge Base) của AI.
- **Triggers:** Cần điều chỉnh hành vi phản hồi của trợ lý ảo hoặc cập nhật dữ liệu địa điểm mới cho AI học.
- **Preconditions:** Super Admin đã đăng nhập thành công (UC06).
- **Post Conditions:** Mọi hội thoại của du khách ở UC05 ngay sau đó sẽ áp dụng cấu hình và tri thức mới.
- **Assumptions:** Dịch vụ AI Engine đang hoạt động ổn định.
- **Reference Business Rules:** BR-09, BR-11

#### Basic Course of Events

1. **Actor:** Super Admin truy cập vào menu _"Quản lý AI"_.
2. **System:** Kiểm tra phiên đăng nhập, hiển thị bảng cấu hình Prompt, danh sách từ khóa cấm và trạng thái đồng bộ dữ liệu (Xem luồng E1).
3. **Actor:** Chỉnh sửa nội dung System Prompt hoặc thêm/bớt từ khóa cấm, sau đó nhấn _"Lưu cấu hình"_.
4. **System:** Kiểm tra kết nối tới AI Engine (Xem luồng E2).
5. **System:** Lưu cấu hình vào cơ sở dữ liệu và áp dụng cho AI Engine. Hiển thị thông báo thành công. Use Case kết thúc chu trình.

#### Alternative Paths

- **A1. Đồng bộ tri thức (Knowledge Base):** Tại bảng điều khiển, Super Admin nhấn _"Đồng bộ dữ liệu POI vào AI"_. Hệ thống trích xuất toàn bộ nội dung văn bản của các địa điểm từ UC08 và nạp vào bộ nhớ của AI Engine.

#### Exception Paths

- **E1. Phiên làm việc hết hạn:** Tại bước 2, nếu Token hết hạn, hệ thống điều hướng về trang Đăng nhập (UC06).
- **E2. Lỗi kết nối AI Engine:** Tại bước 4 của luồng cơ bản hoặc luồng A1, nếu dịch vụ AI gặp sự cố, hệ thống giữ nguyên cấu hình cũ và thông báo: _"Không thể kết nối đến AI Engine. Vui lòng thử lại sau"_.
