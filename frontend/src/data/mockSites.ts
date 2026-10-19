export interface SiteData {
  id: number;
  name: string;
  description: string;
  imageUrl: string;
  address: string;
  audioUrl: string;
}

export const mockSites: Record<number, SiteData> = {
  1: {
    id: 1,
    name: "Landmark 81",
    description: "Tòa nhà cao nhất Việt Nam với đài quan sát SkyView.",
    imageUrl: "https://en.wikipedia.org/wiki/Landmark_81",
    address: "Thành phố Hồ Chí Minh",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
  },
  2: {
    id: 2,
    name: "Dinh Độc Lập",
    description:
      "Di tích quốc gia đặc biệt, nơi kết thúc chiến tranh Việt Nam.",
    imageUrl: "https://vi.wikipedia.org/wiki/Dinh_%C4%90%E1%BB%99c_L%E1%BA%ADp",
    address: "Thành phố Hồ Chí Minh",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
  },
  3: {
    id: 3,
    name: "Chợ Bến Thành",
    description: "Biểu tượng thương mại lâu đời nhất Sài Gòn.",
    imageUrl: "https://images.unsplash.com/photo-1583417319070-4a69db38a482",
    address: "Thành phố Hồ Chí Minh",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
  },
};
