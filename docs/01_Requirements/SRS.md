TÀI LIỆU ĐẶC TẢ YÊU CẦU PHẦN MỀM (SRS)
Dự án: LinguaFlow - Hệ thống thuyết minh tự động đa ngôn ngữ
Kiến trúc: 3-Tier (React.js, Spring Boot, SQL Server)

# 1. GIỚI THIỆU (INTRODUCTION)

## 1.1. Mục đích (Purpose)

Tài liệu này đặc tả các yêu cầu chức năng và phi chức năng của hệ thống LinguaFlow dựa trên Tài liệu Yêu cầu Sản phẩm (PRD). Các yêu cầu được tổ chức và chi tiết hóa từ các Business Rules và Use Cases trong PRD. Những ràng buộc và lựa chọn kỹ thuật bổ sung được phân biệt rõ với yêu cầu nghiệp vụ gốc. Tài liệu này là cơ sở cho việc thiết kế kiến trúc, mô hình dữ liệu (ERD), API và triển khai hệ thống.

## 1.2. Phạm vi (Scope)

LinguaFlow là ứng dụng Web (PWA/Web-based) hỗ trợ khách tham quan tự động nghe thuyết minh dựa trên tọa độ vị trí (GPS) và tương tác với AI. Hệ thống chia làm 2 phân hệ:

- Phân hệ Du khách (Visitor): Đồng bộ dữ liệu offline, xem bản đồ, nghe thuyết minh tự động qua Geofencing/QR, và hỏi đáp với Trợ lý AI.
- Phân hệ Quản trị (Super Admin duy nhất): Thống kê hiệu suất, quản lý điểm đến (POI) và cấu hình tri thức cho hệ thống AI.

# 2. MÔ TẢ TỔNG QUAN (OVERALL DESCRIPTION)

## 2.1. Phân loại người dùng (User Classes)

- Khách tham quan (Visitor): Sử dụng trình duyệt di động, cấp quyền định vị GPS, có nhu cầu tra cứu bản đồ và nghe thuyết minh tự động.
- Super Admin duy nhất: Tài khoản quản trị duy nhất `admin`, có quyền cấu hình hệ thống, quản lý dữ liệu POI, duyệt bản dịch và nạp dữ liệu cho AI.
- Hệ thống AI (System Actor): Các background worker xử lý dịch thuật, sinh file Audio (TTS) và động cơ RAG trả lời Chatbot.

## 2.2. Ràng buộc thiết kế (Constraints)

- **CON-01 (Offline-First và đồng bộ theo phân khu):** Trong lần truy cập đầu tiên, hệ thống phải đồng bộ dữ liệu POI cơ bản, bao gồm tọa độ, bán kính kích hoạt và thông tin cần thiết để xác định phân khu. Dữ liệu này phải được lưu trữ cục bộ bằng IndexedDB để hỗ trợ định vị GPS khi kết nối mạng không ổn định hoặc bị gián đoạn. Khi xác định được phân khu hiện tại bằng GPS hoặc QR, hệ thống phải tải toàn bộ Audio thuộc phân khu đó theo ngôn ngữ thuyết minh du khách đang chọn và lưu vào bộ nhớ đệm trên thiết bị. Không yêu cầu tải Audio của tất cả phân khu ngay từ lần truy cập đầu tiên.

- **CON-02 (Quản lý tài nguyên):** Admin chỉ được tải lên hình ảnh với dung lượng tối đa 5MB. Audio thuyết minh được tạo tự động bằng dịch vụ Text-to-Speech (TTS) trên máy chủ, không yêu cầu Admin tải MP3 lên. SQL Server chỉ lưu đường dẫn và thông tin quản lý Audio, không lưu dữ liệu nhị phân của tệp Audio.

- **CON-03 (Xử lý bất đồng bộ):** Các tác vụ dịch thuật AI và tạo Audio bằng TTS phải được xử lý bất đồng bộ thông qua hàng đợi tác vụ hoặc cơ chế xử lý nền. Việc tạo Audio không được thực hiện đồng bộ trong quá trình du khách yêu cầu phát thuyết minh.

# 3. YÊU CẦU CHỨC NĂNG (FUNCTIONAL REQUIREMENTS - FR)

## Nhóm 3.1: Phân hệ Du khách (Visitor)

- **FR-VIS-01 [UC01]:** Hệ thống tải giao diện và đồng bộ dữ liệu POI cơ bản trong lần truy cập đầu tiên, bao gồm tọa độ, bán kính kích hoạt và thông tin cần thiết để xác định phân khu của từng POI. Dữ liệu được lưu trữ cục bộ bằng IndexedDB để phục vụ định vị GPS và hoạt động khi kết nối mạng không ổn định. Trong các lần truy cập tiếp theo, hệ thống kiểm tra DataVersion để xác định có cần cập nhật dữ liệu hay không. Hệ thống không tải Audio của tất cả phân khu ngay từ đầu; Audio được tải theo phân khu sau khi xác định được phân khu hiện tại bằng GPS hoặc QR.

- FR-VIS-02 [UC2]: Hệ thống cung cấp bản đồ số, hiển thị các điểm tham quan (Marker), vị trí hiện tại của du khách khi có quyền truy cập GPS và vòng tròn thể hiện bán kính vùng kích hoạt (Geofence) của từng POI.

- **FR-VIS-03 [UC03]:** Hệ thống cho phép du khách lựa chọn riêng ngôn ngữ giao diện và ngôn ngữ thuyết minh. Tài nguyên giao diện đa ngôn ngữ được quản lý ở phía Client thông qua các tệp locale. Khi du khách thay đổi ngôn ngữ thuyết minh, hệ thống sử dụng ngôn ngữ mới cho nội dung thuyết minh, Audio và phản hồi của AI Chatbot. Khi một phân khu đã được xác định, hệ thống tải toàn bộ Audio thuộc phân khu đó theo ngôn ngữ thuyết minh đang chọn và lưu vào bộ nhớ đệm. Hệ thống không tải Audio của các phân khu khác nếu chưa được xác định hoặc yêu cầu. Nếu không thể tải nội dung hoặc Audio do mất kết nối mạng, hệ thống thông báo lỗi và giữ nguyên dữ liệu đã có trong bộ nhớ đệm.

- **FR-VIS-04 [UC04]:** Hệ thống hỗ trợ thuyết minh tự động bằng Geofencing dựa trên vị trí GPS của du khách và bán kính kích hoạt được cấu hình cho từng POI. Việc tính khoảng cách bằng thuật toán Haversine và lọc nhiễu GPS phải được thực hiện hoàn toàn phía Client. Hệ thống bỏ qua tín hiệu GPS có sai số lớn hơn 150m và áp dụng hysteresis 40m để hạn chế kích hoạt lặp lại khi vị trí dao động quanh ranh giới Geofence.
  - Kiểm tra hàng chờ âm thanh để tránh phát chồng chéo nhiều địa điểm.
  - Áp dụng Cooldown 5 phút cho từng POI đã được phát tự động bằng GPS.
  - Quét QR hoặc chọn trực tiếp POI trên bản đồ phải có độ ưu tiên cao hơn cơ chế kích hoạt GPS.
  - Khi GPS xác định được phân khu, Client tải toàn bộ Audio thuộc phân khu đó theo ngôn ngữ thuyết minh đang chọn. Khi du khách quét QR, hệ thống xác định POI từ mã QR, suy ra phân khu chứa POI và tự động tải toàn bộ Audio của phân khu đó theo ngôn ngữ đang chọn.
  - Audio được kiểm tra trong bộ nhớ cục bộ trước. Nếu chưa có hoặc phiên bản đã cũ, Client tải tệp Audio tương ứng từ máy chủ, lưu vào bộ nhớ đệm và phát sau khi tải thành công.
  - Quá trình tạo Audio bằng TTS được thực hiện trên máy chủ, không thực hiện đồng bộ trong quá trình du khách yêu cầu phát. Việc tải trước Audio theo phân khu chỉ lấy các tệp Audio đã được tạo và sẵn sàng.
  - Nếu Audio chưa được tạo hoặc chưa sẵn sàng, hệ thống thông báo cho du khách và không sử dụng tệp Audio cũ không còn tương ứng với nội dung hiện tại. Hệ thống không được phát nhầm Audio của ngôn ngữ khác.

- **FR-VIS-05 [UC05]:** Trợ lý Chat AI cho phép du khách đặt câu hỏi chuyên sâu về du lịch, lịch sử và văn hóa. Hệ thống gửi câu hỏi kèm ngữ cảnh cần thiết, bao gồm ngôn ngữ đang chọn và vị trí hiện tại nếu có, đến AI Engine xử lý. AI phải trả lời theo ngôn ngữ du khách đang chọn và từ chối các yêu cầu vi phạm chính sách nội dung hoặc chứa từ khóa bị cấm.
  - Lịch sử hội thoại cá nhân được lưu trữ cục bộ trên thiết bị du khách.
  - Hệ thống chỉ gửi dữ liệu thống kê sử dụng ở dạng tổng hợp về máy chủ, không mặc định lưu toàn bộ nội dung câu hỏi và câu trả lời của từng cuộc hội thoại.
  - Du khách có thể chủ động chọn một đoạn hội thoại để gửi phản hồi hoặc báo cáo câu trả lời AI không phù hợp. Hệ thống phải cho phép du khách xem lại nội dung trước khi xác nhận gửi.

## Nhóm 3.2: Phân hệ Quản trị (Admin)

- **FR-ADM-01 [UC06]:** Hệ thống chỉ tồn tại duy nhất một tài khoản Super Admin với tên đăng nhập cố định `admin`. Tài khoản được tự động khởi tạo khi hệ thống chạy lần đầu với mật khẩu mặc định `123456` đã được mã hóa BCrypt và phải hỗ trợ chức năng đổi mật khẩu. Hệ thống không cung cấp chức năng đăng ký tài khoản mới hoặc quên mật khẩu qua email. Phiên đăng nhập phải tự động hết hạn sau 30 phút không có thao tác hợp lệ; thời gian không hoạt động phải được kiểm tra ở phía máy chủ. Nếu nhập sai mật khẩu quá 5 lần liên tiếp, chức năng đăng nhập phải bị khóa tạm thời trong 15 phút.

- **FR-ADM-02 [UC07]:** Dashboard hiển thị các chỉ số hoạt động theo thời gian thực và dữ liệu lịch sử, có khả năng lọc theo khoảng thời gian. Các chỉ số thời gian thực phải được tách biệt:
  - Số phiên truy cập website đang hoạt động (Online Visitors), bao gồm cả du khách không cấp quyền GPS.
  - Số phiên GPS đang hoạt động (Active GPS Sessions), chỉ bao gồm các phiên đã được cấp quyền GPS và đang gửi cập nhật vị trí.
  - Số liệu lịch sử bao gồm lượt quét QR, lượt nghe Audio, mức độ sử dụng Chatbot, các POI được quan tâm nhiều nhất và phản hồi do du khách tự nguyện gửi.
  - Trạng thái online và GPS được xác định dựa trên heartbeat hoặc thời điểm hoạt động gần nhất của phiên truy cập. Việc thống kê không yêu cầu xác định danh tính thực của du khách. Dashboard không mặc định hiển thị lịch sử hội thoại cá nhân.

- **FR-ADM-03 [UC08]:** Hệ thống cung cấp chức năng quản lý POI (CRUD), cho phép Super Admin quản lý thông tin địa điểm bao gồm tọa độ Lat/Lng, bán kính kích hoạt, tên, mô tả, hình ảnh, mã QR và nội dung thuyết minh đa ngôn ngữ. Hình ảnh tải lên không được vượt quá 5MB. Admin không tải MP3 lên hệ thống.
  - Mỗi POI có duy nhất một bài thuyết minh hỗ trợ nhiều ngôn ngữ.
  - Nội dung tiếng Việt (`vi-VN`) và tiếng Anh Mỹ (`en-US`) do Admin biên soạn thủ công. Nội dung tiếng Anh Mỹ là nguồn để AI dịch sang các ngôn ngữ bổ sung.
  - Hệ thống khởi tạo 7 ngôn ngữ: `vi-VN`, `en-US`, `zh-CN`, `ja-JP`, `hi-IN`, `fr-FR`, `ar`.
  - Super Admin có thể bật hoặc tắt các ngôn ngữ bổ sung theo từng POI.
  - Nội dung dịch bằng AI phải được Super Admin kiểm tra và duyệt trước khi đưa vào sử dụng.
  - Sau khi nội dung được duyệt, hệ thống đưa tác vụ tạo Audio bằng TTS vào hàng đợi xử lý nền.
  - Trạng thái dịch thuật và trạng thái tạo Audio phải được quản lý độc lập.
  - Khi nội dung thuộc phạm vi gói đồng bộ POI thay đổi, hệ thống tăng DataVersion. Việc tạo lại Audio hoặc thay đổi trạng thái TTS không tự động làm tăng DataVersion nếu không làm thay đổi dữ liệu thuộc gói đồng bộ.

- **FR-ADM-04 [UC09]:** Hệ thống cho phép Super Admin thiết lập System Prompt, quản lý danh sách từ khóa cấm (Banned Keywords) và yêu cầu đồng bộ dữ liệu POI vào Knowledge Base. Nội dung POI và nội dung thuyết minh đã được duyệt phải được xử lý và cập nhật vào cơ sở tri thức phục vụ cơ chế truy xuất thông tin RAG. Quá trình này không đồng nghĩa với việc huấn luyện lại mô hình AI.

## Nhóm 3.3: Động cơ AI ngầm (System Engine)

- **FR-AI-01 (Quản lý dịch thuật):** Nội dung tiếng Việt (`vi-VN`) và tiếng Anh Mỹ (`en-US`) do Super Admin biên soạn thủ công; `en-US` là nguồn dịch chuẩn cho các ngôn ngữ bổ sung.
  - Đối với quy trình quản trị nội dung, hệ thống hỗ trợ Super Admin tạo bản dịch AI cho các ngôn ngữ được bật theo từng POI. Các bản dịch này phải được lưu vào cơ sở dữ liệu và chờ Super Admin kiểm tra, duyệt trước khi được công bố theo quy trình quản trị.
  - Đối với yêu cầu của du khách, nếu nội dung ở ngôn ngữ thuyết minh đang chọn chưa tồn tại, hệ thống tự động yêu cầu AI dịch từ nội dung `en-US` và lưu kết quả vào cơ sở dữ liệu để sử dụng cho các lần truy cập sau. Bản dịch tự động phát sinh theo yêu cầu này được phép trả về cho du khách khi tạo thành công, không phải chờ quy trình duyệt nội dung quản trị.
  - Nếu không thể tạo bản dịch theo yêu cầu, hệ thống trả nội dung `en-US` làm ngôn ngữ dự phòng. Hệ thống phải ghi nhận trạng thái hoặc lỗi dịch để có thể thử lại; không được trả về nội dung dịch cũ không còn tương ứng với phiên bản `en-US` hiện tại.

- **FR-AI-02 (Quản lý tác vụ TTS):** Sau khi nội dung của một ngôn ngữ được duyệt theo quy trình quản trị, hệ thống tự động tạo tác vụ Text-to-Speech cho ngôn ngữ đó và đưa vào hàng đợi xử lý nền. Đối với bản dịch tự động phát sinh theo yêu cầu du khách, hệ thống cũng phải đưa tác vụ TTS vào hàng đợi để tạo Audio tương ứng. Tệp Audio được tạo và lưu trữ trên máy chủ hoặc dịch vụ lưu trữ tệp riêng. SQL Server chỉ lưu đường dẫn và trạng thái Audio. Trạng thái Audio phải được quản lý độc lập với trạng thái dịch thuật. Trong khi Audio của ngôn ngữ đã chọn chưa sẵn sàng, hệ thống áp dụng nội dung/Audio `en-US` làm phương án dự phòng nếu có sẵn; không được chặn yêu cầu phát để chờ TTS hoàn tất.

- **FR-AI-03 (Đồng bộ phiên bản nội dung và Audio):** Hệ thống phải đảm bảo Audio được phát tương ứng với phiên bản nội dung thuyết minh hiện tại. Khi nội dung thuyết minh thay đổi hoặc bản dịch tự động được tạo mới/cập nhật, Audio tương ứng phải được đánh dấu cần tạo hoặc tạo lại. Không được sử dụng Audio cũ nếu nội dung của nó không còn tương ứng với nội dung hiện tại. Khi tải Audio theo phân khu, Client phải kiểm tra phiên bản hoặc định danh nội dung để chỉ tải mới những tệp còn thiếu hoặc đã thay đổi.

# 4. YÊU CẦU PHI CHỨC NĂNG (NON-FUNCTIONAL REQUIREMENTS - NFR)

- **NFR-PF-01 (Hiệu năng):** Các thao tác kiểm tra vị trí GPS, tính khoảng cách Haversine, lọc nhiễu và xác định POI thuộc vùng Geofence phải được thực hiện phía Client. Việc phát Audio không được chờ quá trình tạo TTS mới; Client chỉ truy xuất Audio đã được tạo sẵn từ máy chủ.

- **NFR-RL-01 (Độ tin cậy):** Các tác vụ dịch thuật AI và tạo Audio bằng TTS phải có cơ chế xử lý lỗi, ghi nhận trạng thái, cho phép thử lại khi nhà cung cấp dịch vụ gặp sự cố. Việc lỗi một tác vụ không được làm mất nội dung thuyết minh đã lưu hoặc ảnh hưởng đến các tác vụ xử lý khác.

- **NFR-SC-01 (Bảo mật):** Các API quản trị phải được bảo vệ bằng Spring Security và cơ chế xác thực JWT. Chỉ Super Admin đã xác thực mới được phép quản lý POI, nội dung thuyết minh, cấu hình AI và xem Dashboard. Các API phục vụ du khách chỉ được cấp quyền truy cập theo đúng phạm vi chức năng, không được để lộ các API quản trị. Mật khẩu Super Admin phải được lưu dưới dạng mã băm BCrypt.

- **NFR-DP-01 (Lưu trữ dữ liệu):** Lịch sử hội thoại cá nhân của du khách được lưu cục bộ trên thiết bị. Máy chủ chỉ lưu thống kê sử dụng tổng hợp và nội dung phản hồi do du khách chủ động gửi. Không được mặc định lưu toàn bộ lịch sử hội thoại cá nhân vào cơ sở dữ liệu.

- **NFR-ST-01 (Lưu trữ Audio):** Tệp Audio phải được lưu ở vị trí lưu trữ bền vững, độc lập với vòng đời triển khai ứng dụng Spring Boot. Cơ sở dữ liệu chỉ lưu thông tin tham chiếu đến tệp Audio, không lưu trực tiếp dữ liệu nhị phân.

- **NFR-NW-01 (Đồng bộ Audio theo phân khu):** Việc tải Audio theo phân khu phải có khả năng tiếp tục hoặc thử lại khi kết nối mạng bị gián đoạn. Client phải tránh tải trùng các tệp đã có phiên bản hợp lệ trong cache; khi đổi ngôn ngữ hoặc phân khu, chỉ tải các Audio còn thiếu hoặc không còn đúng phiên bản. Hệ thống phải thông báo trạng thái tải và không làm gián đoạn Audio đang phát chỉ vì tác vụ tải trước thất bại.

- **NFR-MON-01 (Thống kê phiên truy cập):** Client phải gửi heartbeat định kỳ để máy chủ xác định phiên truy cập website còn hoạt động. Heartbeat GPS chỉ được ghi nhận khi du khách đã cấp quyền định vị và Client thực sự gửi cập nhật vị trí. Ngưỡng thời gian xác định một phiên còn hoạt động phải được cấu hình thống nhất ở phía máy chủ; không được đồng nhất số phiên truy cập website với số phiên GPS.
