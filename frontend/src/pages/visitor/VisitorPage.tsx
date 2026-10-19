import React, { useState } from "react";
import { MapPin, Globe, Headphones, Clock, Ticket } from "lucide-react";

// Cấu trúc dữ liệu cho một Điểm đến (POI)
interface POI {
  id: number;
  name: string;
  category: string;
  address: string;
  description: string;
  imageUrl: string;
  color: string; // Màu sắc chủ đạo cho từng POI
}

// Danh sách các POI mẫu để hiển thị đầy đủ
const POI_LIST: POI[] = [
  {
    id: 1,
    name: "Dinh Độc Lập",
    category: "Di tích Lịch sử",
    address: "135 Nam Kỳ Khởi Nghĩa, Quận 1",
    description:
      "Nơi lưu giữ những dấu ấn quan trọng của lịch sử Việt Nam hiện đại với kiến trúc độc đáo.",
    imageUrl:
      "https://upload.wikimedia.org/wikipedia/commons/d/d0/Dinh_%C4%90%E1%BB%99c_L%E1%BA%ADp_v%C3%A0o_n%C3%A0m_2024.jpg?utm_source=vi.wikipedia.org&utm_campaign=index&utm_content=original",
    color: "bg-emerald-500",
  },
  {
    id: 2,
    name: "Nhà thờ Đức Bà",
    category: "Kiến trúc Tôn giáo",
    address: "01 Công xã Paris, Quận 1",
    description:
      "Kiệt tác kiến trúc Gothic cổ điển xây dựng bằng gạch đỏ vận chuyển từ Pháp.",
    imageUrl:
      "https://cdn.xanhsm.com/2024/11/f5d8fc3e-nha-tho-duc-ba-thumbnail-min-1.jpg",
    color: "bg-rose-500",
  },
  {
    id: 3,
    name: "Bảo tàng Mỹ thuật",
    category: "Nghệ thuật & Văn hóa",
    address: "97 Phó Đức Chính, Quận 1",
    description:
      "Tòa nhà kiến trúc Pháp kết hợp Á Đông với nhiều góc trưng bày nghệ thuật độc đáo.",
    imageUrl:
      "https://lh7-us.googleusercontent.com/-vlIMB_NiDf3Yi3hnDkC899xviQjU9Lpji6cjGbo4a0BZtbhuaAMJnuwIFEXSuqSaiega8GeSJx_O8oDLxNnQyMYveMpm7XgqIsUhMudO3F8i09y7HYvNxKo5EsXKQPF9rH4fQCAlhZ9NMNDbE0au3Q",
    color: "bg-amber-500",
  },
  {
    id: 4,
    name: "Bưu điện Thành phố",
    category: "Kiến trúc/Check-in",
    address: "2 Công trường Công xã Paris (quận 1)",
    description: "Sự kết hợp giữa phong cách châu Âu và nét Á Đông",
    imageUrl:
      "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQxE0cjTZnqgcsAdnAlly5VcO0nJvy1qlnZpxM-RlrxBw&s=10",
    color: "bg-rose-500",
  },
  {
    id: 5,
    name: "Phố đi bộ Nguyễn Huệ",
    category: "Năng động/Chill",
    address: "trung tâm Quận 1, Thành phố Hồ Chí Minh",
    description: "Không gian sinh hoạt cộng đồng náo nhiệt nhất thành phố",
    imageUrl: "https://api.sovaba.travel/uploads/pho_di_bo_hue_3a9c03dc24.jpg",
    color: "bg-rose-500",
  },
  {
    id: 6,
    name: "Bảo tàng Chứng tích Chiến tranh",
    category: "Lịch sử Giáo dục",
    address: "01 Công xã Paris, Quận 1",
    description: "Nơi lưu giữ những hình ảnh chân thực về chiến tranh",
    imageUrl:
      "https://cdn.xanhsm.com/2024/11/f5d8fc3e-nha-tho-duc-ba-thumbnail-min-1.jpg",
    color: "bg-rose-500",
  },
  {
    id: 7,
    name: "Thảo Cầm Viên",
    category: "Thiên nhiên/Gia đình",
    address: "01 Công xã Paris, Quận 1",
    description:
      "Kiệt tác kiến trúc Gothic cổ điển xây dựng bằng gạch đỏ vận chuyển từ Pháp.",
    imageUrl:
      "https://cdn.xanhsm.com/2024/11/f5d8fc3e-nha-tho-duc-ba-thumbnail-min-1.jpg",
    color: "bg-rose-500",
  },
  {
    id: 8,
    name: "Nhà thờ Đức Bà",
    category: "Kiến trúc Tôn giáo",
    address: "01 Công xã Paris, Quận 1",
    description:
      "Kiệt tác kiến trúc Gothic cổ điển xây dựng bằng gạch đỏ vận chuyển từ Pháp.",
    imageUrl:
      "https://cdn.xanhsm.com/2024/11/f5d8fc3e-nha-tho-duc-ba-thumbnail-min-1.jpg",
    color: "bg-rose-500",
  },
  {
    id: 9,
    name: "Nhà thờ Đức Bà",
    category: "Kiến trúc Tôn giáo",
    address: "01 Công xã Paris, Quận 1",
    description:
      "Kiệt tác kiến trúc Gothic cổ điển xây dựng bằng gạch đỏ vận chuyển từ Pháp.",
    imageUrl:
      "https://cdn.xanhsm.com/2024/11/f5d8fc3e-nha-tho-duc-ba-thumbnail-min-1.jpg",
    color: "bg-rose-500",
  },
  {
    id: 10,
    name: "Nhà thờ Đức Bà",
    category: "Kiến trúc Tôn giáo",
    address: "01 Công xã Paris, Quận 1",
    description:
      "Kiệt tác kiến trúc Gothic cổ điển xây dựng bằng gạch đỏ vận chuyển từ Pháp.",
    imageUrl:
      "https://cdn.xanhsm.com/2024/11/f5d8fc3e-nha-tho-duc-ba-thumbnail-min-1.jpg",
    color: "bg-rose-500",
  },
];

export const VisitorPage: React.FC = () => {
  const [selectedLanguage, setSelectedLanguage] = useState("vi");
  const [activePoi, setActivePoi] = useState<POI>(POI_LIST[0]);

  return (
    <div className="min-h-screen bg-slate-50 font-sans pb-24">
      {/* Header nhiều màu sắc và chọn ngôn ngữ */}
      <header className="bg-white sticky top-0 z-20 px-5 py-4 shadow-sm flex justify-between items-center border-b border-slate-100">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-2xl flex items-center justify-center text-white font-black text-xl shadow-lg shadow-blue-200">
            L
          </div>
          <div>
            <h1 className="text-xl font-extrabold text-slate-800 tracking-tight">
              LinguaFlow
            </h1>
            <p className="text-xs font-medium text-slate-500">
              Khám phá thành phố
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 bg-slate-100/80 hover:bg-slate-200 transition-colors px-3 py-2 rounded-xl">
          <Globe className="w-4 h-4 text-slate-600" />
          <select
            value={selectedLanguage}
            onChange={(e) => setSelectedLanguage(e.target.value)}
            className="bg-transparent text-sm font-semibold text-slate-700 outline-none cursor-pointer appearance-none pr-2"
          >
            <option value="vi">🇻🇳 Tiếng Việt</option>
            <option value="en">EN English</option>
            <option value="ja">🇯🇵 日本語</option>
            <option value="kr">kr 한국</option>
          </select>
        </div>
      </header>

      <main className="p-4 max-w-lg mx-auto space-y-6 mt-2">
        {/* Thẻ hiển thị chi tiết POI đang được chọn */}
        <section className="bg-white rounded-3xl p-4 shadow-xl shadow-slate-200/50 border border-slate-100 transition-all duration-300">
          <div className="relative overflow-hidden rounded-2xl h-64 mb-5 group">
            <img
              src={activePoi.imageUrl}
              alt={activePoi.name}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
            />
            <div className="absolute top-3 left-3 flex gap-2">
              <span
                className={`px-3 py-1.5 text-white text-xs font-bold rounded-xl shadow-md backdrop-blur-md ${activePoi.color}`}
              >
                {activePoi.category}
              </span>
            </div>
          </div>

          <div className="px-2 space-y-4">
            <div>
              <h2 className="text-3xl font-extrabold text-slate-800 mb-2">
                {activePoi.name}
              </h2>
              <div className="flex items-center gap-2 text-slate-500 text-sm font-medium">
                <MapPin className="w-4 h-4 text-blue-500" />
                {activePoi.address}
              </div>
            </div>

            <p className="text-slate-600 text-base leading-relaxed bg-slate-50 p-4 rounded-2xl border border-slate-100">
              {activePoi.description}
            </p>

            <button className="w-full bg-slate-900 hover:bg-slate-800 text-white font-bold py-4 rounded-2xl flex items-center justify-center gap-2 transition-transform active:scale-95">
              <Headphones className="w-5 h-5" />
              Nghe thuyết minh ngay
            </button>
          </div>
        </section>

        {/* Danh sách các POI khác */}
        <section>
          <h3 className="text-lg font-bold text-slate-800 mb-4 px-2">
            Các địa điểm lân cận
          </h3>
          <div className="flex gap-4 overflow-x-auto pb-4 snap-x pr-4 pl-2 hide-scrollbar">
            {POI_LIST.map((poi) => (
              <div
                key={poi.id}
                onClick={() => setActivePoi(poi)}
                className={`snap-start shrink-0 w-64 bg-white rounded-2xl p-3 cursor-pointer transition-all border-2 ${activePoi.id === poi.id ? "border-blue-500 shadow-md shadow-blue-100" : "border-transparent shadow-sm hover:shadow-md"}`}
              >
                <img
                  src={poi.imageUrl}
                  alt={poi.name}
                  className="w-full h-32 object-cover rounded-xl mb-3"
                />
                <h4 className="font-bold text-slate-800 text-base line-clamp-1">
                  {poi.name}
                </h4>
                <p className="text-xs text-slate-500 mt-1 flex items-center gap-1">
                  <MapPin className="w-3 h-3" /> {poi.category}
                </p>
              </div>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
};

export default VisitorPage;
