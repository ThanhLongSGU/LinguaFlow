const STORAGE_KEY = "linguaflow:narration-cooldowns";
export const NARRATION_COOLDOWN_MS = 300_000;

type CooldownRecord = Record<string, number>;

function readCooldowns(): CooldownRecord {
  if (typeof window === "undefined") return {};

  try {
    const stored = window.localStorage.getItem(STORAGE_KEY);
    if (!stored) return {};
    const parsed: unknown = JSON.parse(stored);
    if (!parsed || typeof parsed !== "object") return {};
    return Object.fromEntries(
      Object.entries(parsed).filter(
        ([, value]) => typeof value === "number" && Number.isFinite(value),
      ),
    );
  } catch {
    return {};
  }
}

function writeCooldowns(cooldowns: CooldownRecord): void {
  if (typeof window === "undefined") return;
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(cooldowns));
  } catch {
    // Storage can be disabled; autoplay still remains safe for this session.
  }
}

export function canAutoPlay(siteId: number, now = Date.now()): boolean {
  const lastPlayedAt = readCooldowns()[String(siteId)];
  return lastPlayedAt === undefined || now - lastPlayedAt >= NARRATION_COOLDOWN_MS;
}

export function markAutoPlayed(siteId: number, playedAt = Date.now()): void {
  const cooldowns = readCooldowns();
  cooldowns[String(siteId)] = playedAt;
  writeCooldowns(cooldowns);
}
