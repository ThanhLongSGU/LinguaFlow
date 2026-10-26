import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import { useLocationTracking } from '../../hooks/useLocationTracking';
import { checkNearbySite } from '../../services/locationService';
import { Navigation, MapPin, Locate, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import L from 'leaflet';
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

// Sửa lỗi Icon mặc định của Leaflet
let DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

interface NearbySite {
  siteId: number;
  name: string;
  address?: string;
  imageUrl?: string;
  distanceMeters?: number;
  distance?: number;
}

const ALL_SITES = [
  { id: 1, name: 'Landmark 81', lat: 10.7951, lng: 106.7218 },
  { id: 2, name: 'Dinh Độc Lập', lat: 10.7769, lng: 106.6953 },
  { id: 3, name: 'Chợ Bến Thành', lat: 10.7725, lng: 106.6980 },
  { id: 4, name: 'Nhà thờ Đức Bà', lat: 10.7798, lng: 106.6990 },
  { id: 5, name: 'Bưu điện Thành phố', lat: 10.7798, lng: 106.6999 },
  { id: 6, name: 'Địa đạo Củ Chi', lat: 11.1428, lng: 106.4623 },
  { id: 7, name: 'Phố đi bộ Nguyễn Huệ', lat: 10.7744, lng: 106.7034 },
  { id: 8, name: 'Bảo tàng Chứng tích Chiến tranh', lat: 10.7781, lng: 106.6921 },
  { id: 9, name: 'Thảo Cầm Viên', lat: 10.7875, lng: 106.7053 },
  { id: 10, name: 'Bitexco Financial Tower', lat: 10.7716, lng: 106.7044 },
  { id: 11, name: 'Phố Tây Bùi Viện', lat: 10.7674, lng: 106.6939 },
  { id: 12, name: 'Khu du lịch Suối Tiên', lat: 10.8631, lng: 106.8048 },
  { id: 13, name: 'Bến Nhà Rồng', lat: 10.7682, lng: 106.7068 },
  { id: 14, name: 'Cầu Ánh Sao', lat: 10.7292, lng: 106.7196 },
  { id: 15, name: 'Nhà hát Thành phố', lat: 10.7766, lng: 106.7031 },
  { id: 16, name: 'Bảo tàng Mỹ thuật TP.HCM', lat: 10.7694, lng: 106.6994 },
  { id: 17, name: 'Chùa Bửu Long', lat: 10.8447, lng: 106.8329 },
  { id: 18, name: 'Công viên Vinhomes Central Park', lat: 10.7936, lng: 106.7203 },
  { id: 19, name: 'Hẻm Bia: Lost in HongKong', lat: 10.7679, lng: 106.6927 },
  { id: 20, name: 'Khu du lịch sinh thái Cần Giờ', lat: 10.4114, lng: 106.9547 }
];

// Component tự động cập nhật lại size khung bản đồ tránh mảng xám & khóa định vị
const MapController = ({ location, autoCenter }: { location: { lat: number; lng: number } | null; autoCenter: boolean }) => {
  const map = useMap();

  useEffect(() => {
    // Sửa lỗi mảng xám xịt Leaflet trên di động bằng cách invalidateSize
    setTimeout(() => {
      map.invalidateSize();
    }, 300);
  }, [map]);

  useEffect(() => {
    if (location && autoCenter) {
      map.flyTo([location.lat, location.lng], map.getZoom(), { animate: true });
    }
  }, [location, autoCenter, map]);

  return null;
};

export const MapPage: React.FC = () => {
  const navigate = useNavigate();
  const { location, error } = useLocationTracking();
  const [detectedSite, setDetectedSite] = useState<NearbySite | null>(null);
  const [autoCenter, setAutoCenter] = useState<boolean>(true);

  const defaultCenter: [number, number] = [10.7769, 106.6953];

  useEffect(() => {
    if (location) {
      checkNearbySite(location.lat, location.lng).then((site: NearbySite | null) => {
        setDetectedSite(site);
      });
    }
  }, [location]);

  return (
    <div className="h-[100dvh] w-full flex flex-col bg-slate-100 overflow-hidden font-sans">
      {/* Header nhỏ gọn tối ưu Mobile */}
      <header className="bg-white/95 backdrop-blur-md px-4 py-3 shadow-sm z-[1000] shrink-0 border-b border-slate-100 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate(-1)}
            className="p-1.5 rounded-xl hover:bg-slate-100 text-slate-600 transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-base font-extrabold text-slate-800 tracking-tight flex items-center gap-1.5">
              <Navigation className="w-4 h-4 text-blue-600" /> Bản đồ GPS Du lịch
            </h1>
            {location ? (
              <p className="text-[11px] text-emerald-600 font-semibold">
                🟢 Đã kết nối định vị thời gian thực
              </p>
            ) : (
              <p className="text-[11px] text-amber-600 font-medium animate-pulse">
                Đang tìm GPS...
              </p>
            )}
          </div>
        </div>
      </header>

      {/* Thông báo lỗi nếu chưa cấp quyền */}
      {error && (
        <div className="bg-red-50 text-red-600 text-xs px-4 py-2 z-[1000] border-b border-red-100 font-medium">
          {error}
        </div>
      )}

      {/* Bản đồ chiếm trọn phần màn hình còn lại */}
      <div className="flex-1 relative z-0 w-full h-full">
        <MapContainer
          center={location ? [location.lat, location.lng] : defaultCenter}
          zoom={14}
          zoomControl={false}
          style={{ height: '100%', width: '100%' }}
        >
          <TileLayer
            attribution='© OpenStreetMap'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          <MapController location={location} autoCenter={autoCenter} />

          {/* Vị trí GPS của người dùng */}
          {location && (
            <>
              <Circle
                center={[location.lat, location.lng]}
                pathOptions={{ fillColor: '#3B82F6', color: '#1D4ED8', fillOpacity: 0.2 }}
                radius={60}
              />
              <Circle
                center={[location.lat, location.lng]}
                pathOptions={{ fillColor: '#2563EB', color: '#FFFFFF', weight: 3, fillOpacity: 1 }}
                radius={10}
              >
                <Popup>📍 Vị trí hiện tại của bạn</Popup>
              </Circle>
            </>
          )}

          {/* 20 POI địa điểm */}
          {ALL_SITES.map((site) => (
            <Marker key={site.id} position={[site.lat, site.lng]}>
              <Popup>
                <div className="text-center p-0.5">
                  <h3 className="font-bold text-slate-800 text-xs">{site.name}</h3>
                  <button
                    onClick={() => navigate(`/visitor/${site.id}`)}
                    className="mt-1.5 bg-blue-600 text-white text-[11px] px-2.5 py-1 rounded-lg font-medium shadow"
                  >
                    Xem thuyết minh
                  </button>
                </div>
              </Popup>
            </Marker>
          ))}
        </MapContainer>

        {/* Nút Khóa vị trí định vị */}
        <button
          onClick={() => setAutoCenter(!autoCenter)}
          className={`absolute bottom-6 right-4 z-[1000] p-3 rounded-2xl shadow-xl border transition-all ${
            autoCenter ? 'bg-blue-600 text-white border-blue-700' : 'bg-white text-slate-700 border-slate-200'
          }`}
        >
          <Locate className="w-5 h-5" />
        </button>

        {/* Popup nhận diện địa điểm tự động */}
        {detectedSite && (
          <div className="absolute top-4 left-1/2 -translate-x-1/2 bg-white/95 backdrop-blur-md px-4 py-3 rounded-2xl shadow-2xl border border-emerald-300 z-[1000] w-11/12 max-w-xs flex items-center justify-between gap-3 animate-bounce">
             <div className="flex items-center gap-2.5">
               <div className="w-8 h-8 bg-emerald-100 rounded-xl flex items-center justify-center text-emerald-600 shrink-0">
                 <MapPin className="w-5 h-5" />
               </div>
               <div>
                  <p className="text-[10px] font-bold text-emerald-600 uppercase">Đã nhận diện!</p>
                  <p className="text-xs font-bold text-slate-800 line-clamp-1">{detectedSite.name}</p>
                  <p className="text-[10px] text-slate-500">Cách bạn {detectedSite.distanceMeters || detectedSite.distance || 0}m</p>
               </div>
             </div>
             <button
               onClick={() => navigate(`/visitor/${detectedSite.siteId}`)}
               className="bg-emerald-600 text-white text-[11px] font-bold px-3 py-1.5 rounded-xl shrink-0 shadow"
             >
               Nghe ngay
             </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default MapPage;