# Tài liệu Đặc tả Yêu cầu Sản phẩm (PRD) - Hệ thống LinguaFlow

## 1. Quy tắc Nghiệp vụ (Business Rules)

- **BR-01:** Dữ liệu POI (tọa độ, bán kính, thông tin cơ bản) bắt buộc phải được đồng bộ và lưu trữ cục bộ (Offline-first) trên thiết bị của du khách ngay trong lần truy cập đầu tiên.
- **BR-02:** Bản đồ phải hiển thị trực quan vị trí hiện tại của người dùng và bán kính vùng kích hoạt (Geofence) của từng địa điểm.
- **BR-03:** Thuật toán định vị (Haversine) và lọc nhiễu GPS (bỏ qua tín hiệu sai số > 150m, hysteresis 40m) phải được tính toán hoàn toàn phía Client để tiết kiệm tài nguyên máy chủ.
- **BR-04:** Hệ thống phải hỗ trợ chuyển đổi đa ngôn ngữ cho cả giao diện (UI) và nội dung thuyết minh (Audio/TTS). Câu lệnh gửi cho AI Chatbot bắt buộc ép AI trả lời bằng ngôn ngữ du khách đang chọn.
- **BR-05:** Cơ chế phát tự động theo GPS phải kiểm tra hàng chờ âm thanh để tránh phát chồng chéo nhiều địa điểm cùng lúc.
- **BR-06:** Quét mã QR hoặc chọn trực tiếp địa điểm trên bản đồ có độ ưu tiên cao hơn GPS, cho phép phát thuyết minh ngay lập tức.
- **BR-07:** Kích hoạt cơ chế Cooldown (5 phút) cho mỗi địa điểm đã phát tự động qua GPS để tránh làm phiền khi du khách đi ra/vào ranh giới liên tục.
- **BR-08:** Giới hạn dung lượng tệp tải lên từ Admin: Tệp hình ảnh tối đa 5MB còn tệp âm thanh sẽ được thiết kế theo hướng TTS tự sinh và nạp vào máy chủ, không yêu cầu Admin tải MP3 lên.
- **BR-09:** Phiên đăng nhập của Super Admin tự động hết hạn sau 30 phút không có thao tác hợp lệ. Thời gian chờ được tính lại sau mỗi thao tác hợp lệ của người dùng. Hệ thống phải kiểm tra thời gian không hoạt động ở phía máy chủ; không chỉ dựa vào thời hạn cố định của JWT.
- **BR-10:** Khóa tạm thời chức năng đăng nhập trong 15 phút nếu nhập sai mật khẩu quá 5 lần liên tiếp.
- **BR-11:** Hệ thống quản trị được thiết kế đóng, chỉ tồn tại duy nhất **01 tài khoản Super Admin**. Tên đăng nhập được khóa cứng là `admin`, mật khẩu khởi tạo mặc định là `123456` (bắt buộc hỗ trợ tính năng đổi mật khẩu). Dữ liệu tài khoản này phải được tự động khởi tạo (Database Seeding) khi hệ thống chạy lần đầu; tuyệt đối không xây dựng API đăng ký tài khoản mới hoặc quên mật khẩu qua email.

---

## 2. Đặc tả Chi tiết Các Use Case (Use Case Specifications)

### UC01: Truy cập và Đồng bộ dữ liệu

- **Actor(s):** Du khách
- **Maturity:** Focused
- **Summary:** Du khách truy cập vào hệ thống. Hệ thống tiến hành tải giao diện và đồng bộ danh sách các địa điểm tham quan (tọa độ, bán kính kích hoạt và thông tin cơ bản) về thiết bị để chuẩn bị cho việc định vị và phát thuyết minh sau đó.
- **Triggers:** Du khách muốn bắt đầu sử dụng phần mềm để nghe thuyết minh du lịch.
- **Preconditions:** Thiết bị phải có kết nối mạng ổn định trong lần khởi chạy đầu tiên.
- **Post Conditions:** Thiết bị đã lưu trữ dữ liệu POI cơ bản, bao gồm tọa độ và bán kính kích hoạt, sẵn sàng cho việc xác định địa điểm bằng GPS ngay cả khi kết nối mạng không ổn định. Nội dung thuyết minh và Audio không bắt buộc phải được tải xuống trong lần đồng bộ đầu tiên.
- **Assumptions:** Trình duyệt của du khách hỗ trợ tính năng lưu trữ dữ liệu cục bộ (IndexedDB/LocalStorage).
- **Reference Business Rules:** BR-01

#### Basic Course of Events

1. **Actor:** Du khách quét QR để truy cập vào hệ thống Hướng dẫn Du lịch TP.HCM.
2. **System:** Tải giao diện người dùng cơ bản.
3. **System:** Kiểm tra dữ liệu lưu trữ trên thiết bị (Xem luồng A1).
4. **System:** Nhận thấy đây là lần truy cập đầu tiên (chưa có dữ liệu), kết nối với máy chủ để tải danh sách POI và các cấu hình cần thiết, bao gồm tọa độ, bán kính kích hoạt và thông tin cơ bản của từng địa điểm (Xem luồng E1, E2).
5. **System:** Lưu trữ dữ liệu POI đã tải vào bộ nhớ cục bộ của thiết bị. Nội dung thuyết minh và tệp Audio sẽ được truy xuất riêng khi du khách có nhu cầu sử dụng.
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
- **Summary:** Du khách thay đổi ngôn ngữ hiển thị của hệ thống. Việc thay đổi này cập nhật giao diện và xác định ngôn ngữ nội dung thuyết minh, Audio và phản hồi của AI Chatbot cho những lần tương tác tiếp theo.
- **Triggers:** Du khách muốn chuyển sang một ngôn ngữ khác để sử dụng ứng dụng.
- **Preconditions:** Hệ thống đã hoàn tất UC01.
- **Post Conditions:** Giao diện và luồng âm thanh thuyết minh tiếp theo sẽ được phát theo ngôn ngữ vừa chọn.
- **Assumptions:** Tài nguyên giao diện đa ngôn ngữ được quản lý ở phía Client. Nội dung thuyết minh và Audio đa ngôn ngữ được quản lý trên máy chủ và được truy xuất theo ngôn ngữ du khách lựa chọn.
- **Reference Business Rules:** BR-04

#### Basic Course of Events

1. **Actor:** Du khách nhấn vào biểu tượng/nút _"Chọn ngôn ngữ"_ trên giao diện.
2. **System:** Hiển thị danh sách các ngôn ngữ được hỗ trợ (Tiếng Việt, Tiếng Anh...).
3. **Actor:** Du khách bấm chọn một ngôn ngữ mới (Xem luồng A1).
4. **System:** Ghi nhận lựa chọn, cập nhật toàn bộ văn bản trên giao diện và sử dụng ngôn ngữ mới làm ngôn ngữ ưu tiên cho nội dung thuyết minh, Audio và AI Chatbot. Audio tương ứng sẽ được tải từ máy chủ khi cần phát.
5. **System:** Hiển thị hoàn tất giao diện với ngôn ngữ mới. Use Case kết thúc thành công.

#### Alternative Paths

- **A1. Hủy chọn ngôn ngữ:** Tại bước 3, du khách bấm ra ngoài hoặc nhấn nút Đóng. Hệ thống đóng danh sách ngôn ngữ, giữ nguyên trạng thái cũ.

#### Exception Paths

- **E1. Mất kết nối khi tải tài nguyên ngôn ngữ:** Nếu thiết bị mất mạng khi cần truy xuất nội dung thuyết minh hoặc Audio của ngôn ngữ mới, hệ thống thông báo lỗi kết nối và giữ nguyên dữ liệu đã lưu trong bộ nhớ đệm. Giao diện vẫn có thể chuyển ngôn ngữ nếu tài nguyên UI tương ứng đã có sẵn trên thiết bị.

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
5. **System:** Kiểm tra Audio tương ứng với ngôn ngữ hiện tại trong bộ nhớ cục bộ. Nếu đã có, hệ thống phát trực tiếp. Nếu chưa có, hệ thống yêu cầu tải tệp Audio đã được tạo sẵn từ máy chủ, lưu vào bộ nhớ đệm cục bộ và phát sau khi tải thành công. Việc tạo Audio bằng TTS được xử lý ở phía máy chủ, không thực hiện đồng bộ trong lúc du khách yêu cầu phát.

#### Alternative Paths

- **A1. Kích hoạt bằng mã QR (Bỏ qua GPS):**
  1. Du khách dùng camera quét mã QR đặt tại điểm tham quan.
  2. Hệ thống nhận diện ID địa điểm từ mã QR, bỏ qua bước 2 và 3 của Luồng cơ bản, ưu tiên xử lý yêu cầu phát thuyết minh và chuyển thẳng đến bước 5.
- **A2. Chọn nghe địa điểm tùy ý:**
  1. Du khách nhấn vào một điểm (marker) trên bản đồ và chọn _"Nghe thuyết minh"_.
  2. Hệ thống bỏ qua bước quét tọa độ, nhảy thẳng tới bước 5 để phát Audio.
- **A3. Phát lại:** Du khách nhấn nút _"Phát lại"_ trên bảng điều khiển media; hệ thống tua file Audio về `0:00` và phát lại từ đầu.

#### Exception Paths

- **E1. Mất tín hiệu GPS:** Tại bước 2, nếu không lấy được tọa độ, hệ thống hiển thị trạng thái _"Đang tìm GPS"_ và tạm ngưng quét Geofence. Tính năng quét QR (A1) vẫn hoạt động bình thường.
- **E2. Chặn spam (Cooldown):** Tại bước 3, nếu du khách liên tục đi ra/vào ranh giới của một địa điểm trong thời gian dưới 5 phút, hệ thống từ chối phát lại Audio của điểm đó.

- **E3. Không tải được Audio:** Nếu Audio chưa có trong bộ nhớ cục bộ và thiết bị không thể tải tệp từ máy chủ, hệ thống thông báo: _"Không thể tải nội dung thuyết minh. Vui lòng kiểm tra kết nối mạng và thử lại"_ (hiển thị thông báo này theo ngôn ngữ mà du khách chọn).
- **E4. Audio chưa được tạo hoặc đang xử lý:** Nếu Audio của ngôn ngữ được chọn chưa được tạo thành công trên máy chủ, hệ thống thông báo nội dung thuyết minh hiện chưa sẵn sàng và không phát một tệp Audio cũ không còn tương ứng với nội dung hiện tại.

---

### UC05: Hỏi chat AI

- **Actor(s):** Du khách
- **Maturity:** Focused
- **Summary:** Du khách tương tác với trợ lý ảo AI để hỏi đáp các thông tin chuyên sâu về du lịch, lịch sử, văn hóa hoặc xin gợi ý lịch trình bằng ngôn ngữ đã chọn.
- **Triggers:** Du khách cần tìm hiểu thêm thông tin ngoài lề mà bản thuyết minh âm thanh chưa đề cập.
- **Preconditions:** Đã cấu hình ngôn ngữ hiển thị (UC03) và thiết bị có kết nối Internet.
- **Post Conditions:** Hệ thống cập nhật lịch sử hội thoại vào bộ nhớ cục bộ của thiết bị. Hệ thống chỉ gửi dữ liệu thống kê sử dụng ở dạng tổng hợp về máy chủ. Du khách có thể chủ động gửi một đoạn hội thoại cụ thể để phản hồi hoặc báo cáo câu trả lời AI không phù hợp.
- **Assumptions:** Máy chủ AI Engine hoạt động ổn định.
- **Reference Business Rules:** BR-04

#### Basic Course of Events

1. **Actor:** Du khách mở giao diện Trợ lý AI và nhập câu hỏi (hoặc chọn câu hỏi gợi ý).
2. **System:** Kiểm tra kết nối mạng Internet (Xem luồng E1).
3. **System:** Đóng gói câu hỏi kèm ngữ cảnh hiện tại (ngôn ngữ đang chọn, tọa độ vị trí) gửi lên AI Engine xử lý.
4. **System:** AI phân tích và trả về câu trả lời (Xem luồng E2).
5. **System:** Hiển thị câu trả lời lên màn hình chat và lưu đoạn hội thoại vào bộ nhớ đệm cục bộ. Use Case kết thúc chu trình.
6. **System:** Cập nhật các số liệu thống kê sử dụng cần thiết ở dạng tổng hợp, không mặc định lưu toàn bộ nội dung câu hỏi và câu trả lời của từng cuộc hội thoại vào cơ sở dữ liệu máy chủ.

#### Alternative Paths

- **A1. Xem lịch sử chat:** Du khách cuộn lên trên hoặc mở mục lịch sử; hệ thống truy xuất bộ nhớ cục bộ và hiển thị các đoạn hội thoại trước đó.
- **A2. Gửi phản hồi về câu trả lời AI:**
  1. Du khách chọn một đoạn hội thoại muốn phản hồi hoặc báo cáo.
  2. Hệ thống hiển thị nội dung sẽ được gửi để du khách xem lại và xác nhận.
  3. Sau khi du khách xác nhận, hệ thống gửi nội dung được chọn cùng thông tin phản hồi cần thiết lên máy chủ để Super Admin xem xét.

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
- **Summary:** Hệ thống tổng hợp và hiển thị các số liệu hoạt động (lượt nghe Audio, lượt quét QR, thống kê sử dụng Chatbot, địa điểm được quan tâm nhất và phản hồi do du khách tự nguyện gửi) để Super Admin theo dõi hiệu suất.
- **Triggers:** Super Admin muốn theo dõi hiệu suất vận hành của hệ thống.
- **Preconditions:** Super Admin đã đăng nhập thành công (UC06).
- **Post Conditions:** Không có sự thay đổi nào về mặt dữ liệu gốc.
- **Assumptions:** Hệ thống ghi nhận các sự kiện tương tác cần thiết dưới dạng thống kê tổng hợp. Nội dung lịch sử hội thoại cá nhân của du khách không được mặc định lưu trữ hoặc hiển thị trên Dashboard.
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
- **Summary:** Super Admin thực hiện thêm mới, cập nhật hoặc xóa thông tin các địa điểm tham quan (tọa độ GPS, bán kính kích hoạt, hình ảnh, mã QR và nội dung thuyết minh đa ngôn ngữ).
- **Triggers:** Có địa điểm tham quan mới hoặc cần cập nhật nội dung thuyết minh.
- **Preconditions:** Super Admin đã đăng nhập thành công (UC06).
- **Post Conditions:** Hệ thống lưu thông tin POI và nội dung thuyết minh. Khi dữ liệu POI cơ bản hoặc nội dung thuyết minh được thay đổi, hệ thống cập nhật phiên bản dữ liệu tương ứng để thiết bị du khách có thể đồng bộ. Các tác vụ dịch thuật và tạo Audio được quản lý độc lập với trạng thái nội dung thuyết minh.
- **Assumptions:**
  - Mỗi POI có duy nhất một bài thuyết minh. Bài thuyết minh hỗ trợ nhiều ngôn ngữ. Nội dung tiếng Việt (`vi-VN`) và tiếng Anh Mỹ (`en-US`) do Super Admin biên soạn thủ công. Các ngôn ngữ bổ sung được dịch bằng AI từ nội dung tiếng Anh Mỹ. Audio của từng ngôn ngữ được tạo bằng TTS trên máy chủ. Admin chỉ tải lên hình ảnh, không tải lên MP3.
  - Ngôn ngữ mặc định: Hệ thống khởi tạo 7 ngôn ngữ:
    - Tiếng Việt (`vi-VN`): Admin biên soạn thủ công.
    - Tiếng Anh Mỹ (`en-US`): Admin biên soạn thủ công và là ngôn ngữ nguồn cho dịch thuật AI.
    - Tiếng Trung giản thể (`zh-CN`).
    - Tiếng Nhật (`ja-JP`).
    - Tiếng Hindi (`hi-IN`).
    - Tiếng Pháp (`fr-FR`).
    - Tiếng Ả Rập chuẩn hiện đại (`ar`).
  - **Cấu hình ngôn ngữ theo POI:** Super Admin có thể bật hoặc tắt các ngôn ngữ bổ sung cho từng POI. Chỉ những ngôn ngữ được bật và có nội dung đã được duyệt mới được xem là sẵn sàng phục vụ du khách.

- **Quy tắc đồng bộ DataVersion:** DataVersion chỉ tăng khi dữ liệu thuộc phạm vi gói đồng bộ POI thay đổi, bao gồm thông tin cơ bản của POI (tên, mô tả, tọa độ, bán kính kích hoạt, hình ảnh, trạng thái hoạt động) hoặc cấu hình ngôn ngữ và nội dung thuyết minh đã được duyệt nếu các dữ liệu này được đưa vào gói đồng bộ. Việc tạo lại Audio hoặc thay đổi trạng thái xử lý TTS không tự động làm tăng DataVersion nếu không làm thay đổi dữ liệu thuộc gói đồng bộ.
- **Reference Business Rules:** BR-08, BR-09, BR-11

#### Basic Course of Events

1. **Actor:** Super Admin truy cập vào menu _"Quản lý POI"_.
2. **System:** Kiểm tra tính hợp lệ của phiên đăng nhập, sau đó hiển thị danh sách các POI hiện có (Xem luồng E1).
3. **Actor:** Super Admin nhấn _"Thêm mới"_ hoặc chọn một POI để chỉnh sửa. Admin nhập thông tin địa điểm (Lat/Lng, bán kính kích hoạt, tên, mô tả), tải lên hình ảnh và quản lý nội dung thuyết minh theo từng ngôn ngữ.
4. **System:** Kiểm tra tính hợp lệ của dữ liệu POI, nội dung thuyết minh và dung lượng hình ảnh (tối đa 5MB). Hệ thống quản lý riêng trạng thái biên soạn, dịch thuật và tạo Audio của từng ngôn ngữ.
5. **Actor:** Nhấn nút _"Lưu"_.
6. **System:** Lưu thông tin vào cơ sở dữ liệu. Nếu dữ liệu thay đổi thuộc phạm vi gói đồng bộ POI, hệ thống tăng DataVersion; sau đó cập nhật lại danh sách POI. Use Case kết thúc chu trình.

#### Alternative Paths

- **A1. Xóa địa điểm (POI):**
  1. Tại danh sách POI, Super Admin chọn một địa điểm và nhấn _"Xóa"_.
  2. Hệ thống kiểm tra ràng buộc (chỉ cho phép xóa nếu địa điểm không còn dính dáng đến mã QR hoặc bài thuyết minh đang hoạt động) và hiển thị hộp thoại xác nhận.
  3. Super Admin xác nhận; hệ thống xóa dữ liệu và tăng phiên bản đồng bộ.

- **A2. Quản lý nội dung thuyết minh đa ngôn ngữ:**
  1. Super Admin biên soạn nội dung tiếng Việt (`vi-VN`) và tiếng Anh Mỹ (`en-US`) thủ công.
  2. Đối với các ngôn ngữ bổ sung, Super Admin yêu cầu hệ thống dịch nội dung từ bản tiếng Anh Mỹ bằng AI.
  3. Hệ thống tạo tác vụ dịch thuật và cập nhật trạng thái xử lý riêng cho từng ngôn ngữ.
  4. Super Admin kiểm tra và duyệt nội dung dịch trước khi đưa vào sử dụng.
  5. Sau khi nội dung được duyệt, hệ thống đưa tác vụ tạo Audio bằng TTS vào hàng đợi xử lý trên máy chủ.
  6. Hệ thống cập nhật trạng thái Audio độc lập với trạng thái dịch thuật. Khi tạo Audio thành công, tệp được lưu trữ trên máy chủ và đường dẫn được cập nhật vào dữ liệu tương ứng.

#### Exception Paths

- **E1. Phiên làm việc hết hạn:** Tại bước 2, nếu Token hết hạn, hệ thống đẩy người dùng về trang Đăng nhập (UC06).
- **E2. Vi phạm dung lượng tệp:** Tại bước 4, nếu hình ảnh vượt quá 5MB, hệ thống chặn thao tác và thông báo: _"File hình ảnh vượt quá dung lượng cho phép"_.

---

### UC09: Quản lý hệ thống AI

- **Actor(s):** Super Admin
- **Maturity:** Focused
- **Summary:** Super Admin cấu hình chỉ dẫn hệ thống (System Prompt), quản lý danh sách từ khóa cấm và kích hoạt đồng bộ nội dung POI vào cơ sở tri thức (Knowledge Base) để AI Chatbot có thể truy xuất thông tin khi trả lời du khách.
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

- **A1. Đồng bộ tri thức (Knowledge Base):** Tại bảng điều khiển, Super Admin nhấn _"Đồng bộ dữ liệu POI vào AI"_. Hệ thống trích xuất nội dung POI và nội dung thuyết minh đã được duyệt, xử lý và cập nhật vào cơ sở tri thức phục vụ cơ chế truy xuất thông tin (RAG). Quá trình này không đồng nghĩa với việc huấn luyện lại mô hình AI.

#### Exception Paths

- **E1. Phiên làm việc hết hạn:** Tại bước 2, nếu Token hết hạn, hệ thống điều hướng về trang Đăng nhập (UC06).
- **E2. Lỗi kết nối AI Engine:** Tại bước 4 của luồng cơ bản hoặc luồng A1, nếu dịch vụ AI gặp sự cố, hệ thống giữ nguyên cấu hình cũ và thông báo: _"Không thể kết nối đến AI Engine. Vui lòng thử lại sau"_.
