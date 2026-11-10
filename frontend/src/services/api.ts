export const apiBaseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";

export interface TranslationItem {
  languageCode: string;
  languageName: string;
  content: string;
  audioUrl: string;
  isDefault: boolean;
}

export interface TouristSiteDetail {
  siteId: number;
  name: string;
  description: string;
  imageUrl: string;
  address: string;
  availableTranslations: TranslationItem[];
}

export interface PoiTranslation {
  languageCode: string;
  audioUrl: string;
}

export interface PoiOfflineData {
  siteId: number;
  name: string;
  address: string;
  description: string;
  latitude: number;
  longitude: number;
  triggerRadius: number;
  imageUrl: string;
  translations: PoiTranslation[];
}

export interface PoiSyncResponse {
  serverDataVersion: number;
  isUpdated: boolean;
  poiList: PoiOfflineData[];
}

export async function getTouristSiteByQr(qrCodeValue: string, signal?: AbortSignal): Promise<TouristSiteDetail> {
  const response = await fetch(`${apiBaseURL}/tourist-sites/qr/${encodeURIComponent(qrCodeValue)}`, { signal });

  if (!response.ok) {
    const message =
      response.status === 404 || response.status === 400
        ? "Mã QR không hợp lệ hoặc địa điểm không còn khả dụng."
        : "Không thể tải thông tin địa điểm. Vui lòng thử lại.";
    throw new Error(message);
  }

  return (await response.json()) as TouristSiteDetail;
}

// Hàm này sẽ dùng để tải toàn bộ POI về một lần (offline mode)
export async function syncPoiData(clientVersion: number = 0, signal?: AbortSignal): Promise<PoiSyncResponse | null> {
  const response = await fetch(`${apiBaseURL}/sync/poi?clientVersion=${encodeURIComponent(clientVersion)}`, { signal });

  if (response.status === 304) return null;

  if (!response.ok) {
    throw new Error(`Không thể đồng bộ địa điểm (HTTP ${response.status}).`);
  }

  return (await response.json()) as PoiSyncResponse;
}

export function resolveAssetUrl(path: string): string {
  try {
    const backendOrigin = apiBaseURL.replace(/\/api\/v1\/?$/, "");
    return new URL(path, `${backendOrigin}/`).toString();
  } catch {
    return path;
  }
}
