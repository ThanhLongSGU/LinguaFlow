PRD
USE CASE TỔNG QUÁT
  



  	Use Case Number: 
	UC1
	Use Case Name:
	Truy cập và Đồng bộ dữ liệu 
	Actor (s):
	Du khách 
	Maturity:
	Focused
	Summary:
	Du khách truy cập vào hệ thống. Hệ thống tiến hành tải giao diện và âm thầm đồng bộ hóa danh sách các địa điểm tham quan (Tọa độ, bán kính quét, thông tin cơ bản) về thiết bị của người dùng để chuẩn bị cho việc định vị và phát thuyết minh sau đó. 
	Basic Course of Events:
	Actor Action
 
	System Response
	1. Du khách quét QR để  truy cập vào hệ thống Hướng dẫn Du lịch TP.HCM. 
	 
	 
	2. Hệ thống tải giao diện người dùng cơ bản. 
	

	3. Hệ thống kiểm tra dữ liệu lưu trữ trên thiết bị. A1 
	

	4. Nhận thấy đây là lần truy cập đầu tiên (chưa có dữ liệu), hệ thống kết nối với máy chủ để tải toàn bộ danh sách địa điểm (POI) và các cấu hình ưu tiên. E1, E2 
	

	5. Hệ thống lưu trữ gói dữ liệu này vào bộ nhớ của thiết bị để phục vụ việc định vị vị trí. 
	

	6. Hệ thống hiển thị bản đồ tổng quan đã đánh dấu các địa điểm. Trạng thái Use Case kết thúc thành công. 
	Alternative Paths:
	A1. Đã có dữ liệu từ lần truy cập trước (Truy cập lần 2 trở đi) 
	Actor Action: 
	System Response: 
	

	1. Tại bước 3 của Luồng cơ bản, hệ thống phát hiện thiết bị đã có sẵn dữ liệu địa điểm. 
	

	2. Hệ thống gửi yêu cầu kiểm tra phiên bản dữ liệu lên máy chủ. 
	

	3. Nếu máy chủ báo "Không có bản cập nhật mới", hệ thống bỏ qua bước tải dữ liệu (bước 4 & 5), sử dụng lại dữ liệu cũ và tiến thẳng đến bước hiển thị bản đồ (bước 6). Nếu có bản cập nhật, hệ thống quay lại chạy bước 4.  
	Exception Paths:
	E1. Không có kết nối mạng (Lần truy cập đầu) 
Tại bước 4 của Luồng cơ bản, nếu thiết bị không có internet, hệ thống không thể tải dữ liệu POI. Hệ thống hiển thị thông báo: "Cần có kết nối mạng để tải dữ liệu ban đầu". Use Case ngưng trệ cho đến khi có mạng. 
E2. Máy chủ không phản hồi 
Tại bước 4, nếu mạng bình thường nhưng máy chủ hệ thống gặp sự cố, hệ thống hiển thị thông báo: "Hệ thống đang bảo trì, vui lòng quay lại sau". Use Case kết thúc thất bại. 
	Extension Points:
	Không có. . 
	Triggers:
	Du khách muốn bắt đầu sử dụng phần mềm để nghe thuyết minh du lịch. . 
	Assumptions:
	Trình duyệt của du khách hỗ trợ tính năng lưu trữ dữ liệu cục bộ trên thiết bị. 
	Preconditions:
	Thiết bị phải có kết nối mạng ổn định trong lần khởi chạy đầu tiên. 
	Post Conditions:
	Hệ thống đã nắm giữ toàn bộ tọa độ và bán kính của các POI, sẵn sàng cho việc kích hoạt "Thuyết minh tự động theo GPS" (UC4) ngay cả khi mạng bị chập chờn sau đó. . 
	Reference: Business Rules:
	BR-1
	Author(s):
	Hoàng Thanh Long 
	Date:
	

	 
 
 
Activity Diagram:


   
	







  	Use Case Number: 
	UC2
	Use Case Name:
	Xem bản đồ và vị trí 
	Actor (s):
	Du khách 
	Maturity:
	Focused
	Summary:
	Du khách tương tác với bản đồ số trên hệ thống để quan sát các địa điểm du lịch, định vị vị trí hiện tại của bản thân và xem thông tin chi tiết của một địa điểm cụ thể.
	Basic Course of Events:
	Actor Action
 
	System Response
	1. Du khách mở hệ thống và chọn chức năng xem bản đồ (hoặc hệ thống tự động hiển thị bản đồ ngay khi mở ứng dụng/web). 
	 
	 
	2. Hệ thống tải dữ liệu bản đồ (Map API) và hiển thị các điểm đánh dấu (marker) của các địa điểm tham quan tại TP.HCM. E1 . 
	

	3. Hệ thống cho phép du khách thao tác vuốt, cuộn, phóng to, thu nhỏ để xem bản đồ tự do. 
	Alternative Paths:
	A1. Xem vị trí hiện tại (Kích hoạt UC1_1)
	Actor Action: 
	System Response: 
	1. Du khách nhấn vào biểu tượng "Vị trí của tôi" (My Location) trên bản đồ. 
	

	

	2. Hệ thống gửi yêu cầu lấy tọa độ GPS của thiết bị. E2
	

	3. Hệ thống định vị thành công, cuộn bản đồ về tọa độ hiện tại và hiển thị biểu tượng "Bạn đang ở đây". Quay lại bước 3 của Luồng cơ bản. 
	A2. Xem thông tin chi tiết địa điểm tham quan (Kích hoạt UC1_2) 
	Actor Action: 
	System Response: 
	1. Du khách nhấn vào một điểm đánh dấu (marker) bất kỳ trên bản đồ. 
	2. Hệ thống Truy xuất dữ liệu nội bộ/cục bộ  và hiển thị bảng thông tin chi tiết của địa điểm đó (tên, hình ảnh/video, mô tả lịch sử, khoảng cách). E3 
	Exception Paths:
	E1. Không tải được dữ liệu bản đồ 
Tại bước 2 của Luồng cơ bản, nếu không có kết nối mạng hoặc Map API lỗi, hệ thống hiển thị bản đồ trống với thông báo: "Không thể tải bản đồ, vui lòng kiểm tra kết nối mạng". 
E2. Du khách từ chối cấp quyền định vị GPS 
Tại bước 2 của luồng A1, nếu trình duyệt/thiết bị từ chối cấp quyền vị trí, hệ thống hiển thị thông báo: "Vui lòng cấp quyền vị trí để sử dụng tính năng này". Hệ thống ngắt luồng A1, quay lại bước 3 của Luồng cơ bản. 
E3. Dữ liệu địa điểm bị lỗi hoặc trống 
Tại bước 2 của luồng A2, nếu dữ liệu cục bộ bị thiếu trường thông tin (VD: chưa có ảnh, chưa có mô tả), hệ thống hiển thị Popup: Dữ liệu đang cập nhật. 
	Extension Points:
	1. Xem vị trí hiện tại (UC1_1): Kích hoạt nếu du khách có nhu cầu biết vị trí đang đứng trên bản đồ (Liên kết luồng A1). 
2. Xem thông tin chi tiết (UC1_2): Kích hoạt khi du khách chọn một địa điểm cụ thể trên bản đồ (Liên kết luồng A2). 
	Triggers:
	Du khách cần tra cứu đường đi, muốn biết các điểm du lịch lân cận hoặc cần đọc thông tin của một địa điểm cụ thể. 
	Assumptions:
	Dịch vụ bản đồ (Google Maps hoặc Mapbox) hoạt động bình thường và thiết bị của du khách có hỗ trợ tính năng định vị GPS. 
	Preconditions:
	Hệ thống đã hoàn tất UC1 (Truy cập và Đồng bộ dữ liệu POI vào thiết bị) 
	Post Conditions:
	Hệ thống hiển thị chính xác vùng bản đồ, vị trí người dùng (nếu có) hoặc thông tin chi tiết của địa điểm được chọn. 
	Reference: Business Rules:
	BR-02, BR-03 
	Author(s):
	Hoàng Thanh Long 
	Date:
	

	 
 
 
Activity Diagram:


   
  

	



  	Use Case Number: 
	UC3
	Use Case Name:
	Chọn ngôn ngữ 
	Actor (s):
	Du khách 
	Maturity:
	Focused
	Summary:
	Du khách thay đổi ngôn ngữ hiển thị của hệ thống. Việc thay đổi này sẽ làm mới (reload) lại toàn bộ giao diện trang web và chuyển đổi nguồn dữ liệu audio thuyết minh sang ngôn ngữ tương ứng. 
	Basic Course of Events:
	Actor Action
 
	System Response
	1. Du khách nhấn vào biểu tượng/nút "Chọn ngôn ngữ" trên giao diện. 
	 
	 
	2. Hệ thống hiển thị danh sách các ngôn ngữ được hỗ trợ (Tiếng Việt, Tiếng Anh...). 
	3. Du khách bấm chọn một ngôn ngữ mới (VD: Tiếng Anh). A1 
	

	

	4. Hệ thống ghi nhận lựa chọn, tiến hành tải lại (reload) trang web. Cập nhật toàn bộ text trên UI và đổi nguồn script/audio thuyết minh sang ngôn ngữ mới. E1 
	

	5. Trang web hiển thị hoàn tất với ngôn ngữ mới. Trạng thái Use Case kết thúc thành công. 
	Alternative Paths:
	A1. Hủy chọn ngôn ngữ 
	Actor Action: 
	System Response: 
	1. Tại bước 3 của Luồng cơ bản, du khách đổi ý không chọn ngôn ngữ mà bấm ra ngoài (hoặc bấm nút Đóng/X). 
	

	

	2. Hệ thống đóng danh sách ngôn ngữ, giữ nguyên trạng thái cũ. Kết thúc Use Case. 
	Exception Paths:
	E1. Mất kết nối khi tải tài nguyên ngôn ngữ 
Tại bước 4 của Luồng cơ bản, nếu mất mạng khi hệ thống đang tải lại trang để kéo gói ngôn ngữ mới, hệ thống sẽ hiển thị thông báo lỗi kết nối của trình duyệt. Khi có mạng lại, trang có thể tự reload vào ngôn ngữ mặc định (hoặc ngôn ngữ đã lưu cache). 
	Extension Points:
	Không có.  
	Triggers:
	Du khách muốn chuyển sang 1 ngôn ngữ khác để sử dụng ap
	Assumptions:
	Dữ liệu dịch thuật UI và các file audio thuyết minh đa ngôn ngữ đã được Admin cấu hình sẵn trên máy chủ. 
	Preconditions:
	Hệ thống đã hoàn tất UC1 (Truy cập và Đồng bộ dữ liệu) 
	Post Conditions:
	Giao diện và luồng âm thanh thuyết minh tiếp theo sẽ được phát theo ngôn ngữ vừa chọn. . 
	Reference: Business Rules:
	BR-04 
	Author(s):
	Hoàng Thanh Long 
	Date:
	

	 
 
 
Activity Diagram:
  

 
	



  	Use Case Number: 
	UC4
	Use Case Name:
	Nghe thuyết minh (GPS & QR Code) 
	Actor (s):
	Du khách 
	Maturity:
	Focused
	Summary:
	Hệ thống tự động phát nội dung thuyết minh dựa trên tọa độ GPS (Geofence) khi du khách di chuyển. Du khách cũng có thể chủ động kích hoạt phát âm thanh bằng cách quét mã QR hoặc chọn trực tiếp trên bản đồ. 
	Basic Course of Events:
	Actor Action
 
	System Response
	1. Du khách bật chế độ "Thuyết minh tự động" và di chuyển. 
	 
	 
	2. Hệ thống (Background Service) liên tục lấy tọa độ GPS của người dùng. 
	

	3. Geofence Engine đối chiếu tọa độ với dữ liệu POI. Nhận diện du khách đã bước vào vùng bán kính kích hoạt của một địa điểm (VD: <20m). E1, E2 
	

	4. Narration Engine kiểm tra trạng thái hàng chờ Audio để chống phát trùng lặp (Debounce & Cooldown).
	

	5. Hệ thống tải gói dữ liệu Audio (hoặc text TTS) tương ứng với ngôn ngữ hiện tại, tự động phát âm thanh và hiển thị bảng điều khiển (Play/Pause/Replay). Trạng thái Use Case kết thúc chu trình. 
	Alternative Paths:
	A1. Kích hoạt bằng mã QR (Bỏ qua GPS) 
	Actor Action: 
	System Response: 
	1. Tại điểm dừng chân (trạm xe buýt, cổng di tích), du khách dùng camera quét mã QR của hệ thống.
	

	

	2. Trình duyệt nhận diện ID địa điểm từ QR, bỏ qua bước 2 và 3 của Luồng cơ bản, nhảy thẳng tới bước 4 để phát Audio ngay lập tức. 
	A2. Chọn nghe địa điểm tùy ý 
	Actor Action: 
	System Response: 
	1. Du khách không chờ GPS mà tự nhấn vào một điểm (marker) trên bản đồ và chọn "Nghe thuyết minh".  
	

	

	2. Hệ thống bỏ qua bước quét tọa độ, nhảy thẳng tới bước 4 để phát Audio. 
	A3. Phát lại 
	Actor Action:
	System Response: 
	1. Du khách nhấn nút "Phát lại" trên bảng điều khiển media. 
	

	

	2. Hệ thống tua file Audio về 0:00 và phát lại từ đầu. 
	Exception Paths:
	E1. Mất tín hiệu GPS 
Tại bước 2 của Luồng cơ bản, nếu không lấy được tọa độ (vào vùng mù GPS), hệ thống hiển thị icon "Đang tìm GPS" và tạm ngưng quét Geofence. Tính năng quét QR (A1) vẫn hoạt động bình thường. 
E2. Chặn spam (Debounce & Cooldown) 
Tại bước 3, nếu du khách liên tục đi ra/đi vào ranh giới 20m của một địa điểm trong thời gian ngắn (VD: dưới 5 phút), hệ thống kích hoạt cơ chế Cooldown, từ chối phát lại Audio của điểm đó để tránh làm phiền. 
	Extension Points:
	Không có.  
	Triggers:
	Du khách đến gần một địa điểm du lịch hoặc đứng chờ tại trạm xe buýt có dán mã QR. 
	Assumptions:
	Dịch vụ Location (GPS) trên thiết bị được cấp quyền chạy ngầm (background). 
	Preconditions:
	Đã tải dữ liệu POI cục bộ (UC1). 
	Post Conditions:
	Hệ thống ghi log "Đã phát" cho POI đó vào bộ nhớ cục bộ để Narration Engine quản lý, sẵn sàng dò tìm điểm tiếp theo. 
	Reference: Business Rules:
	BR-03, BR-04, BR-05, BR-06, BR-07
	Author(s):
	Hoàng Thanh Long 
	Date:
	

	Activity Diagram:
  

  

	

  	Use Case Number: 
	UC05
	Use Case Name:
	Hỏi chat AI 
	Actor (s):
	Du khách 
	Maturity:
	Focused
	Summary:
	Du khách tương tác với trợ lý ảo AI để hỏi đáp các thông tin chuyên sâu về du lịch, lịch sử, văn hóa hoặc xin gợi ý lịch trình. Hệ thống sẽ trả lời bằng ngôn ngữ mà du khách đã chọn. 
	Basic Course of Events:
	Actor Action
 
	System Response
	1. Du khách mở giao diện Trợ lý AI và nhập câu hỏi (hoặc bấm chọn bong bóng câu hỏi gợi ý). 
	 
	 
	2. Hệ thống kiểm tra kết nối mạng Internet. E1 
	

	3. Hệ thống đóng gói câu hỏi, kèm theo Context hiện tại (Ngôn ngữ đang chọn, Tọa độ vị trí) gửi lên AI Engine xử lý. 
	

	4. AI phân tích và trả về câu trả lời. Nếu phát hiện nội dung độc hại hoặc không liên quan du lịch, AI từ chối trả lời. E2 
	

	5. Hệ thống hiển thị câu trả lời lên màn hình chat và lưu đoạn hội thoại vào bộ nhớ đệm (lịch sử). Trạng thái Use Case kết thúc chu trình. 
	Alternative Paths:
	A1. Xem lịch sử chat (Kích hoạt UC5_1) 
	Actor Action: 
	System Response: 
	1. Du khách nhấn vào biểu tượng "Lịch sử chat" (hoặc cuộn ngược lên trên). 
	

	

	2. Hệ thống truy xuất bộ nhớ cục bộ (Local Storage/Cache) và hiển thị các đoạn hội thoại trước đó. 
	Exception Paths:
	E1. Mất kết nối mạng Internet 
Tại bước 2, do AI cần xử lý qua Server/Cloud, nếu thiết bị mất mạng, hệ thống chặn thao tác gửi và hiển thị thông báo: "Vui lòng kết nối Internet để sử dụng Trợ lý AI". 
E2. Vi phạm chính sách nội dung 
Tại bước 4, nếu câu hỏi chứa từ khóa nhạy cảm, chính trị, hoặc không liên quan (VD: "Giải toán giúp tôi"), AI Engine trả về thông báo lỗi: "Xin lỗi, tôi chỉ có thể hỗ trợ các thông tin liên quan đến trải nghiệm du lịch TP.HCM." 
	Extension Points:
	1. Xem lịch sử chat (UC5_1): Kích hoạt khi khách muốn xem lại thông tin cũ (A1). 
	Triggers:
	Du khách cần thêm thông tin ngoài lề (VD: Quán ăn ngon gần di tích này, giờ mở cửa di tích,...) mà file Audio thuyết minh chưa đề cập. 
	Assumptions:
	Thiết bị phải có Internet (3G/4G/Wifi). 
	Preconditions:
	Đã cấu hình xong ngôn ngữ hiển thị (UC3). 
	Post Conditions:
	Hệ thống cập nhật lịch sử chat vào bộ nhớ cục bộ thiết bị. 
	Reference: Business Rules:
	BR-04: Câu lệnh Prompt ngầm gửi cho AI bắt buộc phải ép AI trả lời bằng đúng ngôn ngữ du khách đang chọn ở UC3.  
	Author(s):
	Hoàng Thanh Long 
	Date:
	

	Activity Diagram:
  

  

	





  	Use Case Number: 
	UC06
	Use Case Name:
	Đăng nhập
	Actor (s):
	

	Maturity:
	Focused
	Summary:
	Admin tiến hành xác thực danh tính để truy cập vào hệ thống Quản lý nội dung (CMS). Use Case này đảm bảo chỉ những người có thẩm quyền mới được phép thao tác với dữ liệu POI và AI. 
	Basic Course of Events:
	Actor Action
 
	System Response
	1. Admin truy cập vào đường dẫn trang quản trị (Admin Portal). 
	 
	 
	2. Hệ thống hiển thị form đăng nhập. 
	3. Admin nhập Tên đăng nhập (Username) và Mật khẩu (Password), sau đó nhấn nút "Đăng nhập". 
	

	

	4. Hệ thống kiểm tra thông tin định danh và đối chiếu với dữ liệu lưu trữ. E1, E2 
	

	5. Xác thực thành công, hệ thống khởi tạo phiên làm việc (Session) cho Admin. 
	

	6. Hệ thống chuyển hướng Admin vào màn hình Thống kê (Dashboard). Trạng thái Use Case kết thúc chu trình thành công. 
	Alternative Paths:
	Không có (Đối với Super Admin, việc cấp lại mật khẩu thường cấu hình trực tiếp qua Database để bảo mật cao nhất, không dùng tính năng "Quên mật khẩu" qua email). 
	Exception Paths:
	E1. Sai thông tin đăng nhập 
Tại bước 4, nếu Tên đăng nhập hoặc Mật khẩu không khớp, hệ thống hiển thị thông báo: "Tên đăng nhập hoặc mật khẩu không chính xác". Số lần nhập sai được cộng thêm 1. Hệ thống xóa trắng ô mật khẩu và yêu cầu Admin nhập lại. 
E2. Tài khoản bị khóa (Vượt quá số lần thử) 
Tại bước 4, nếu Admin đã nhập sai quá số lần quy định, hệ thống chặn hành động kiểm tra và hiển thị cảnh báo: "Tài khoản tạm khóa do nhập sai quá nhiều lần. Vui lòng thử lại sau 15 phút". 
	Extension Points:
	Không có. . 
	Triggers:
	Admin cần vào hệ thống để thêm mới, sửa đổi địa điểm tham quan hoặc kiểm tra dữ liệu. 
	Assumptions:
	Admin đã được cấp sẵn tài khoản hợp lệ từ trước. 
	Preconditions:
	Không có. 
	Post Conditions:
	Phiên đăng nhập (Session) được tạo ra. Admin có toàn quyền truy cập các Use Case nhóm Quản lý. 
	Reference: Business Rules:
	BR-09: Phiên đăng nhập tự động hết hạn sau 30 phút không thao tác.
BR-10: Khóa tài khoản 15 phút nếu nhập sai quá 5 lần liên tiếp. 
	Author(s):
	Hoàng Thanh Long 
	Date:
	

	

Activity Diagram:
   
	







  	Use Case Number: 
	UC07
	Use Case Name:
	Xem thống kê (Dashboard) 
	Actor (s):
	Admin (Quản trị viên) 
	Maturity:
	Focused
	Summary:
	Hệ thống tổng hợp và hiển thị các số liệu hoạt động (Lượt nghe audio, lượt quét QR, thống kê chat AI, địa điểm được quan tâm nhất...) trong thời điểm hiện tại và tổng thời gian dưới dạng biểu đồ và danh sách để Admin theo dõi hiệu suất 
	Basic Course of Events:
	Actor Action
 
	System Response
	1. Admin truy cập vào menu "Dashboard" (hoặc được chuyển hướng tự động sau khi đăng nhập). 
	 
	 
	2. Hệ thống kiểm tra tính hợp lệ của phiên làm việc (Session). E1  
	

	3. Hệ thống truy vấn đồng thời 2 luồng dữ liệu: Dữ liệu thời gian thực (Số khách đang online, số phiên GPS đang chạy) và Dữ liệu lịch sử mặc định (Biểu đồ 7 ngày gần nhất). E2 
	

	

	4. Hệ thống hiển thị Dashboard bao gồm: Các thẻ số liệu (Card) Real-time ở trên cùng và Biểu đồ/Bảng xếp hạng ở bên dưới. 
	Alternative Paths:
	A1. Lọc dữ liệu theo mốc thời gian
	Actor Action: 
	System Response: 
	1. Tại bước 4, Admin chọn lại mốc thời gian (VD: Tháng này, Năm nay, hoặc tùy chỉnh từ ngày - đến ngày, hiện tại,...) và nhấn "Lọc". 
	

	

	2. Hệ thống ghi nhận tham số thời gian mới, vòng lại Bước 3 để truy vấn và load lại biểu đồ. Vẫn giữ nguyên phần số liệu Real-time ở trên cùng. 
	Exception Paths:
	E1. Phiên làm việc hết hạn 
Tại bước 2, nếu Admin treo máy quá 30 phút, Session không còn hợp lệ, hệ thống chặn truy cập, hiển thị thông báo "Phiên đăng nhập đã hết hạn" và đẩy Admin văng ra ngoài trang Đăng nhập (Yêu cầu làm lại UC6).
E2. Lỗi truy xuất hoặc Dữ liệu rỗng 
Tại bước 3, nếu lỗi kết nối database hoặc hệ thống mới vận hành chưa có ai sử dụng, hệ thống hiển thị giao diện biểu đồ trống kèm dòng chữ: "Chưa có dữ liệu thống kê trong khoảng thời gian này" hoặc “Hệ thống bị lỗi kết nối”.
	Extension Points:
	Không có.
	Triggers:
	Admin muốn báo cáo hiệu suất hoặc xem địa điểm nào đang thu hút khách nhất. 
	Assumptions:
	Không có. 
	Preconditions:
	Admin đã thực hiện thành công UC6 (Đăng nhập) và đang ở trong khu vực Quản trị. 
	Post Conditions:
	Không có sự thay đổi nào về mặt dữ liệu gốc. 
	Reference: Business Rules:
	BR-09: Phiên làm việc tự động hết hạn sau 30 phút không thao tác. 
	Author(s):
	Hoàng Thanh Long 
	Date:
	

	Activity Diagram:
  

  

  

	





  	Use Case Number: 
	UC08
	Use Case Name:
	Quản lý nội dung POI (Point of Interest) 
	Actor (s):
	Admi
	Maturity:
	Focused
	Summary:
	Admin tiến hành thêm mới, cập nhật hoặc xóa thông tin của các địa điểm tham quan (Tọa độ GPS, Bán kính, Hình ảnh, và File Audio/Script thuyết minh đa ngôn ngữ). 
	Basic Course of Events:
	Actor Action
 
	System Response
	1. Admin truy cập vào menu "Quản lý POI". 
	 
	 
	2. Hệ thống kiểm tra Session (BR-09), sau đó hiển thị danh sách các POI hiện có. E1 
	3. Admin nhấn "Thêm mới" (hoặc chọn 1 POI để Sửa), nhập các thông tin: Tọa độ (Lat/Lng), Bán kính, Tên, Mô tả, và tải lên (upload) File Audio/Ảnh minh họa. 
	

	

	4. Hệ thống kiểm tra tính hợp lệ của dữ liệu nhập và dung lượng các file tải lên. E2
	5. Admin nhấn nút "Lưu". 
	

	

	6. Hệ thống lưu dữ liệu vào cơ sở dữ liệu, cập nhật lại phiên bản dữ liệu (để trigger cho UC1 của du khách tải về bản mới). Hệ thống hiển thị lại danh sách POI. Trạng thái Use Case kết thúc chu trình. 
	Alternative Paths:
	A1. Xóa địa điểm (Xóa POI)
	Actor Action: 
	System Response: 
	1. Tại danh sách POI (Bước 2), Admin chọn một địa điểm và nhấn "Xóa".
	

	

	2. Hệ thống hiển thị popup yêu cầu xác nhận. 
	3. Admin nhấn "Đồng ý". 
	

	

	4. Hệ thống xóa dữ liệu của POI đó, cập nhật lại danh sách hiển thị và tăng phiên bản dữ liệu hệ thống. 
	Exception Paths:
	E1. Phiên làm việc hết hạn 
Tại bước 2, nếu Session hết hạn, hệ thống báo lỗi và văng ra trang Đăng nhập (Yêu cầu làm lại UC6).
E2. Vi phạm dung lượng file (BR-08) 
Tại bước 4, nếu Admin tải lên file Audio > 20MB hoặc Ảnh > 5MB, hệ thống chặn thao tác, hiển thị thông báo: "File tải lên vượt quá dung lượng cho phép". Hệ thống giữ nguyên form nhập liệu để Admin chọn lại file khác. 
	Extension Points:
	Không có. 
	Triggers:
	Có địa điểm tham quan mới được đưa vào khai thác, hoặc cần cập nhật lại kịch bản thuyết minh. 
	Assumptions:
	Admin có sẵn các file Audio chất lượng cao đã thu âm xong. 
	Preconditions:
	Admin đã đăng nhập thành công (UC6). 
	Post Conditions:
	Hệ thống đánh dấu cờ "Cập nhật" để các thiết bị của Du khách tiến hành tải lại gói dữ liệu POI offline khi họ truy cập (UC1). 
	Reference: Business Rules:
	BR-08: Giới hạn dung lượng file Upload (Audio < 20MB, Ảnh < 5MB) 
BR-09: Phiên làm việc (Session) hết hạn sau 30 phút. 
	Author(s):
	Hoàng Thanh Long 
	Date:
	

	Activity Diagram:
  

  

	

  	Use Case Number: 
	UC09
	Use Case Name:
	Quản lý hệ thống AI 
	Actor (s):
	Admin
	Maturity:
	Focused
	Summary:
	Admin thiết lập "tính cách" (System Prompt), quản lý danh sách từ khóa nhạy cảm (cấm hỏi) và kích hoạt đồng bộ dữ liệu địa điểm (POI) vào bộ não (Knowledge Base) của AI để trợ lý ảo luôn cập nhật thông tin mới nhất. 
	Basic Course of Events:
	Actor Action
 
	System Response
	1. Admin truy cập vào menu "Quản lý AI". 
	 
	 
	2. Hệ thống kiểm tra Session (BR-09), sau đó hiển thị bảng điều khiển AI gồm: Cấu hình Prompt, Danh sách từ khóa cấm, và Trạng thái dữ liệu. E1
	3. Admin chỉnh sửa nội dung System Prompt (VD: "Bạn là hướng dẫn viên thân thiện...") hoặc thêm/bớt các từ khóa cấm, sau đó nhấn "Lưu cấu hình".
	

	

	4. Hệ thống kiểm tra kết nối tới AI Engine (API). E2
	

	5. Hệ thống lưu cấu hình vào cơ sở dữ liệu và nạp lại cấu hình này cho AI Engine. Hệ thống thông báo thành công và hiển thị lại giao diện. Trạng thái Use Case kết thúc chu trình. 
	Alternative Paths:
	A1. Đồng bộ tri thức (Cập nhật dữ liệu POI cho AI) 
	Actor Action: 
	System Response: 
	1. Tại bảng điều khiển, Admin nhấn nút "Đồng bộ dữ liệu POI vào AI".
	

	

	2. Hệ thống gom toàn bộ dữ liệu text (tên, mô tả, lịch sử) của các điểm tham quan từ UC8, chuyển đổi và nạp vào Knowledge Base (bộ nhớ) của AI Engine. 
	

	3. Hoàn tất, hệ thống thông báo "Đồng bộ tri thức thành công". 
	Exception Paths:
	E1. Phiên làm việc hết hạn 
Tại bước 2, nếu Session hết hạn, hệ thống văng ra trang Đăng nhập (Yêu cầu làm lại UC6). 
E2. Lỗi kết nối AI Engine (API Error)
Tại bước 4 của luồng cơ bản (hoặc bước 2 của luồng A1), nếu máy chủ AI bên thứ 3 gặp sự cố, hệ thống chặn thao tác và hiển thị Popup: "Không thể kết nối đến AI Engine. Vui lòng thử lại sau". Dữ liệu cũ vẫn được giữ nguyên. 
	Extension Points:
	Không có.  
	Triggers:
	Admin vừa thêm một địa điểm tham quan mới ở UC8 và cần AI "học" ngay thông tin đó để trả lời khách; hoặc khi phát hiện khách hay hỏi những chủ đề cấm (chính trị, giải toán...) cần chặn lại. 
	Assumptions:
	Máy chủ AI Engine đang hoạt động ổn định. 
	Preconditions:
	Admin đã đăng nhập thành công (UC6). 
	Post Conditions:
	Mọi đoạn chat của Du khách ở UC5 ngay sau đó sẽ lập tức áp dụng bộ lọc và tri thức mới này. 
	Reference: Business Rules:
	BR-09: Phiên làm việc tự động hết hạn sau 30 phút. 
	Author(s):
	Hoàng Thanh Long 
	Date:
	

	 Activity Diagram: