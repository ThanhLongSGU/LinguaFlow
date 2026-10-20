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

export function resolveAssetUrl(path: string): string {
  try {
    const backendOrigin = apiBaseURL.replace(/\/api\/v1\/?$/, "");
    return new URL(path, `${backendOrigin}/`).toString();
  } catch {
    return path;
  }
}
