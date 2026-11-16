import { useCallback, useEffect, useSyncExternalStore } from "react";
import { markAutoPlayed } from "../services/narrationCooldownStore";

export interface NarrationPlayerState {
  currentSiteId: number | null;
  currentUrl: string | null;
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  error: string | null;
}

const EMPTY_STATE: NarrationPlayerState = {
  currentSiteId: null,
  currentUrl: null,
  isPlaying: false,
  currentTime: 0,
  duration: 0,
  error: null,
};

class NarrationPlayer {
  private readonly audio = new Audio();
  private state = EMPTY_STATE;
  private readonly listeners = new Set<() => void>();

  constructor() {
    this.audio.addEventListener("timeupdate", () => {
      this.update({ currentTime: this.audio.currentTime });
    });
    this.audio.addEventListener("loadedmetadata", () => {
      this.update({ duration: Number.isFinite(this.audio.duration) ? this.audio.duration : 0 });
    });
    this.audio.addEventListener("ended", () => {
      this.update({ isPlaying: false, currentTime: this.audio.duration || 0 });
    });
    this.audio.addEventListener("error", () => {
      this.update({ isPlaying: false, error: "Không thể phát audio cho địa điểm này." });
    });
  }

  subscribe = (listener: () => void): (() => void) => {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  };

  getSnapshot = (): NarrationPlayerState => this.state;

  async play(siteId: number, url: string, forcePlay = false): Promise<void> {
    if (!forcePlay && this.state.currentSiteId === siteId && this.state.currentUrl === url && this.state.isPlaying) {
      return;
    }

    this.audio.pause();
    this.audio.currentTime = 0;
    this.audio.src = url;
    this.update({ currentSiteId: siteId, currentUrl: url, currentTime: 0, error: null });

    try {
      await this.audio.play();
      if (!forcePlay) markAutoPlayed(siteId);
      this.update({ isPlaying: true });
    } catch (error) {
      this.update({
        isPlaying: false,
        error: error instanceof DOMException && error.name === "NotAllowedError"
          ? "Trình duyệt chặn phát tự động. Hãy bấm Play để nghe thuyết minh."
          : "Không thể phát audio cho địa điểm này.",
      });
      throw error;
    }
  }

  pause = (): void => {
    this.audio.pause();
    this.update({ isPlaying: false });
  };

  replay = (): void => {
    this.audio.currentTime = 0;
    void this.audio.play().then(() => this.update({ isPlaying: true })).catch(() => this.update({ isPlaying: false }));
  };

  stop = (): void => {
    this.audio.pause();
    this.audio.removeAttribute("src");
    this.audio.load();
    this.update({ ...EMPTY_STATE });
  };

  private update(patch: Partial<NarrationPlayerState>): void {
    this.state = { ...this.state, ...patch };
    this.listeners.forEach((listener) => listener());
  }
}

const player = new NarrationPlayer();

export function useNarrationPlayer() {
  const state = useSyncExternalStore(player.subscribe, player.getSnapshot, () => EMPTY_STATE);

  useEffect(() => () => player.stop(), []);

  const play = useCallback((siteId: number, url: string, forcePlay = false) => player.play(siteId, url, forcePlay), []);
  return { ...state, play, pause: player.pause, replay: player.replay, stop: player.stop };
}
