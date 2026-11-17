export const supportedLanguages = ["vi-VN", "en-US", "zh-CN", "ja-JP", "hi-IN", "fr-FR", "ar"] as const;

export type Language = (typeof supportedLanguages)[number];

export function languageMatches(availableLanguage: string, selectedLanguage: Language): boolean {
  const available = availableLanguage.toLowerCase();
  const selected = selectedLanguage.toLowerCase();
  return available === selected || available.split("-")[0] === selected.split("-")[0];
}

export interface TranslationDictionary {
  app: {
    brand: string;
    offlineGuide: string;
  };
  map: {
    title: string;
    synced: string;
    destinations: string;
    destinationCount: string;
    loadingPois: string;
    noPois: string;
    gpsReceiving: string;
    gpsWaiting: string;
    gpsUnsupported: string;
    gpsPermission: string;
    gpsUnavailable: string;
    gpsAccuracy: string;
    userHere: string;
    myLocation: string;
    backToDestination: string;
    dataVersion: string;
    autoPlayOn: string;
    autoPlayOff: string;
    qrScan: string;
    selectDestination: string;
    listen: string;
    nearby: string;
    distance: string;
    activeGuide: string;
    selectedGuide: string;
    noAudio: string;
    syncReadError: string;
    syncOffline: string;
    syncUnavailable: string;
  };
  audio: {
    play: string;
    pause: string;
    replay: string;
    autoplayBlocked: string;
    playbackError: string;
    timeSeconds: string;
  };
  qr: {
    camera: string;
    title: string;
    close: string;
    help: string;
    closeCamera: string;
    cameraError: string;
    offlineError: string;
  };
  language: {
    selectorLabel: string;
    vietnamese: string;
    english: string;
    chinese: string;
    japanese: string;
    hindi: string;
    french: string;
    arabic: string;
  };
  visitor: {
    mapLink: string;
    audioGuide: string;
    destination: string;
    introduction: string;
    listenNarration: string;
    chooseLanguage: string;
    browserAudioUnsupported: string;
    audioUnavailable: string;
    noNarration: string;
    noDescription: string;
    loading: string;
    notFound: string;
    invalidQr: string;
    loadError: string;
  };
}

const vi: TranslationDictionary = {
  app: { brand: "LINGUAFLOW", offlineGuide: "HƯỚNG DẪN NGOẠI TUYẾN" },
  map: {
    title: "Khám phá quanh bạn",
    synced: "ĐÃ ĐỒNG BỘ",
    destinations: "Điểm đến",
    destinationCount: "điểm đến",
    loadingPois: "Đang tải gói địa điểm...",
    noPois: "Chưa có gói POI trên thiết bị. Kết nối mạng để đồng bộ.",
    gpsReceiving: "Đang nhận GPS",
    gpsWaiting: "Đang chờ GPS",
    gpsUnsupported: "Trình duyệt không hỗ trợ định vị GPS.",
    gpsPermission: "Hãy cho phép truy cập vị trí để bật thuyết minh theo GPS.",
    gpsUnavailable: "Chưa lấy được vị trí GPS. Bạn vẫn có thể xem bản đồ và các điểm đến.",
    gpsAccuracy: "Sai số GPS khoảng {meters} m",
    userHere: "Bạn đang ở đây (sai số ±{meters} m)",
    myLocation: "Vị trí của tôi",
    backToDestination: "Về điểm đến",
    dataVersion: "POI v{version}",
    autoPlayOn: "Tắt thuyết minh tự động",
    autoPlayOff: "Bật thuyết minh tự động",
    qrScan: "Quét mã QR",
    selectDestination: "Chọn điểm này",
    listen: "Nghe thuyết minh",
    nearby: "ĐANG Ở ĐÂY",
    distance: "{meters} m từ vị trí của bạn",
    activeGuide: "ĐÃ VÀO VÙNG THUYẾT MINH",
    selectedGuide: "THUYẾT MINH ĐANG CHỌN",
    noAudio: "Địa điểm này chưa có bản thuyết minh audio.",
    syncReadError: "Không đọc được gói POI đã lưu trên thiết bị.",
    syncOffline: "Đang dùng dữ liệu offline.",
    syncUnavailable: "Không thể đồng bộ POI.",
  },
  audio: {
    play: "Phát",
    pause: "Tạm dừng",
    replay: "Phát lại",
    autoplayBlocked: "Trình duyệt chặn phát tự động. Hãy bấm Phát để nghe thuyết minh.",
    playbackError: "Không thể phát audio cho địa điểm này.",
    timeSeconds: "{current}s / {duration}s",
  },
  qr: {
    camera: "CAMERA",
    title: "Quét mã QR",
    close: "Đóng",
    help: "Đưa mã QR vào khung hình để mở thuyết minh.",
    closeCamera: "Đóng camera",
    cameraError: "Không thể truy cập camera. Hãy cấp quyền camera hoặc dùng thiết bị có camera.",
    offlineError: "Không thể đọc địa điểm từ mã QR khi đang offline.",
  },
  language: {
    selectorLabel: "Chọn ngôn ngữ",
    vietnamese: "Tiếng Việt",
    english: "English",
    chinese: "中文",
    japanese: "日本語",
    hindi: "हिन्दी",
    french: "Français",
    arabic: "العربية",
  },
  visitor: {
    mapLink: "Bản đồ GPS",
    audioGuide: "THUYẾT MINH ÂM THANH",
    destination: "ĐIỂM ĐẾN",
    introduction: "GIỚI THIỆU",
    listenNarration: "NGHE THUYẾT MINH",
    chooseLanguage: "Chọn ngôn ngữ",
    browserAudioUnsupported: "Trình duyệt của bạn không hỗ trợ phát audio.",
    audioUnavailable: "Audio chưa sẵn sàng.",
    noNarration: "Chưa có bản thuyết minh cho địa điểm này.",
    noDescription: "Chưa có nội dung giới thiệu cho địa điểm này.",
    loading: "Đang tải thông tin điểm đến...",
    notFound: "Không tìm thấy điểm đến",
    invalidQr: "Mã QR không hợp lệ hoặc địa điểm hiện không khả dụng.",
    loadError: "Đã xảy ra lỗi khi tải dữ liệu.",
  },
};

const en: TranslationDictionary = {
  app: { brand: "LINGUAFLOW", offlineGuide: "OFFLINE GUIDE" },
  map: {
    title: "Explore around you",
    synced: "SYNCED",
    destinations: "Destinations",
    destinationCount: "destinations",
    loadingPois: "Loading destinations...",
    noPois: "No POI package on this device. Connect to sync.",
    gpsReceiving: "GPS active",
    gpsWaiting: "Waiting for GPS",
    gpsUnsupported: "This browser does not support GPS.",
    gpsPermission: "Allow location access to enable GPS narration.",
    gpsUnavailable: "GPS is unavailable. You can still browse the map and destinations.",
    gpsAccuracy: "GPS accuracy about {meters} m",
    userHere: "You are here (accuracy ±{meters} m)",
    myLocation: "My location",
    backToDestination: "Back to destination",
    dataVersion: "POI v{version}",
    autoPlayOn: "Disable auto narration",
    autoPlayOff: "Enable auto narration",
    qrScan: "Scan QR code",
    selectDestination: "Select this place",
    listen: "Listen to narration",
    nearby: "YOU ARE HERE",
    distance: "{meters} m from your location",
    activeGuide: "NARRATION ZONE",
    selectedGuide: "SELECTED NARRATION",
    noAudio: "No narration audio is available for this place.",
    syncReadError: "Could not read the saved POI package.",
    syncOffline: "Using offline data.",
    syncUnavailable: "Could not sync POIs.",
  },
  audio: {
    play: "Play",
    pause: "Pause",
    replay: "Replay",
    autoplayBlocked: "Autoplay was blocked. Press Play to listen.",
    playbackError: "Unable to play audio for this place.",
    timeSeconds: "{current}s / {duration}s",
  },
  qr: {
    camera: "CAMERA",
    title: "Scan QR code",
    close: "Close",
    help: "Place the QR code inside the frame to open narration.",
    closeCamera: "Close camera",
    cameraError: "Could not access the camera. Allow permission or use a device with a camera.",
    offlineError: "This QR location is not available while offline.",
  },
  language: {
    selectorLabel: "Choose language",
    vietnamese: "Tiếng Việt",
    english: "English",
    chinese: "中文",
    japanese: "日本語",
    hindi: "हिन्दी",
    french: "Français",
    arabic: "العربية",
  },
  visitor: {
    mapLink: "GPS map",
    audioGuide: "AUDIO GUIDE",
    destination: "DESTINATION",
    introduction: "INTRODUCTION",
    listenNarration: "LISTEN TO NARRATION",
    chooseLanguage: "Choose language",
    browserAudioUnsupported: "Your browser does not support audio playback.",
    audioUnavailable: "Audio is not ready.",
    noNarration: "No narration is available for this place.",
    noDescription: "No introduction is available for this place.",
    loading: "Loading destination information...",
    notFound: "Destination not found",
    invalidQr: "The QR code is invalid or this destination is unavailable.",
    loadError: "An error occurred while loading the data.",
  },
};

export const translations: Record<Language, TranslationDictionary> = {
  "vi-VN": vi,
  "en-US": en,
  "zh-CN": en,
  "ja-JP": en,
  "hi-IN": en,
  "fr-FR": en,
  ar: en,
};

export const languageLabels: Record<Language, string> = {
  "vi-VN": vi.language.vietnamese,
  "en-US": en.language.english,
  "zh-CN": vi.language.chinese,
  "ja-JP": vi.language.japanese,
  "hi-IN": vi.language.hindi,
  "fr-FR": vi.language.french,
  ar: vi.language.arabic,
};
