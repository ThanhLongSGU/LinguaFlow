import { useState, useEffect } from 'react';

interface LocationCoords {
  lat: number;
  lng: number;
}

export const useLocationTracking = () => {
  const [location, setLocation] = useState<LocationCoords | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!navigator.geolocation) {
      setError("Trình duyệt không hỗ trợ định vị GPS.");
      return;
    }

    // Lắng nghe sự thay đổi tọa độ liên tục khi người dùng di chuyển
    const watchId = navigator.geolocation.watchPosition(
      (position) => {
        setLocation({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        });
        setError(null);
      },
      (err) => {
        console.error("Lỗi lấy GPS:", err);
        setError("Vui lòng cho phép truy cập vị trí (Location Permission) trên trình duyệt.");
      },
      {
        enableHighAccuracy: true, // Ép dùng GPS chính xác cao
        maximumAge: 0,
        timeout: 10000
      }
    );

    return () => navigator.geolocation.clearWatch(watchId);
  }, []);

  return { location, error };
};