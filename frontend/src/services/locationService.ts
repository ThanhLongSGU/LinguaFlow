import api from './api';

// Hàm gọi API lấy địa điểm gần nhất từ Spring Boot
export const checkNearbySite = async (lat: number, lng: number) => {
  try {
    const response = await api.get(`/locations/nearby`, {
      params: { lat, lng }
    });
    return response.data;
  } catch (error) {
    // Trả về null khi đứng xa địa điểm (HTTP 404)
    return null;
  }
};