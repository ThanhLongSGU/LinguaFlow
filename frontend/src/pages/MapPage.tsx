import { Fragment, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Circle, CircleMarker, MapContainer, Popup, TileLayer, useMap } from "react-leaflet";
import {
  getRegionAudioManifest,
  getTouristSiteByQr,
  resolveAssetUrl,
  syncPoiData,
  type PoiOfflineData,
} from "../services/api";
import { readPoiSnapshot, savePoiSnapshot } from "../services/poiOfflineStore";
import QRScannerModal from "../components/QRScannerModal";
import LanguageSelector from "../components/LanguageSelector";
import { useLanguage } from "../contexts/LanguageContext";
import { canAutoPlay } from "../services/narrationCooldownStore";
import { useNarrationPlayer } from "../hooks/useNarrationPlayer";
import {
  calculateHaversineDistance,
  updateGeofenceState,
  type Coordinates,
  type GeofenceState,
} from "../utils/geoUtils";

const DEFAULT_CENTER: Coordinates = { latitude: 10.7769, longitude: 106.6953 };
const EMPTY_GEOFENCE: GeofenceState = { activeSiteId: null, candidateSiteId: null };

function FollowMapCenter({ center }: { center: Coordinates }) {
  const map = useMap();

  useEffect(() => {
    map.flyTo([center.latitude, center.longitude], map.getZoom(), { duration: 0.6 });
  }, [center.latitude, center.longitude, map]);

  return null;
}

function MapPage() {
  const { language, narrationLanguage, t } = useLanguage();
  const player = useNarrationPlayer();
  const [poiList, setPoiList] = useState<PoiOfflineData[]>([]);
  const [dataVersion, setDataVersion] = useState(0);
  const [selectedSiteId, setSelectedSiteId] = useState<number | null>(null);
  const [userLocation, setUserLocation] = useState<Coordinates | null>(null);
  const [mapFocus, setMapFocus] = useState<"site" | "user">("site");
  const [geofence, setGeofence] = useState<GeofenceState>(EMPTY_GEOFENCE);
  const [loadingPois, setLoadingPois] = useState(true);
  const [syncMessage, setSyncMessage] = useState<string | null>(null);
  const [autoPlayEnabled, setAutoPlayEnabled] = useState(false);
  const [isQrScannerOpen, setIsQrScannerOpen] = useState(false);
  const [playingSite, setPlayingSite] = useState<PoiOfflineData | null>(null);
  const [locationMessage, setLocationMessage] = useState<string | null>(() =>
    typeof navigator !== "undefined" && !navigator.geolocation ? t("map", "gpsUnsupported") : null,
  );
  const geofenceRef = useRef<GeofenceState>(EMPTY_GEOFENCE);
  const poiListRef = useRef<PoiOfflineData[]>([]);
  const userLocationRef = useRef<Coordinates | null>(null);
  const autoPlayEnabledRef = useRef(false);
  const previousLanguageRef = useRef(language);
  const playerPlay = player.play;
  const getPreferredTranslation = useCallback(
    (site: PoiOfflineData) =>
      site.translations.find((item) => item.languageCode.toLowerCase() === narrationLanguage) ??
      site.translations.find((item) => item.languageCode.toLowerCase() === "vi") ??
      site.translations[0],
    [narrationLanguage],
  );

  const playSiteAudio = useCallback(
    async (site: PoiOfflineData, forcePlay: boolean) => {
      const translation = getPreferredTranslation(site);
      if (!translation?.audioUrl) {
        setSyncMessage(t("map", "noAudio"));
        return;
      }

      setPlayingSite(site);
      setSelectedSiteId(site.siteId);
      try {
        await playerPlay(site.siteId, resolveAssetUrl(translation.audioUrl), forcePlay);
      } catch {
        // The player exposes the actionable browser error in its control panel.
      }
    },
    [getPreferredTranslation, playerPlay, t],
  );

  useEffect(() => {
    if (previousLanguageRef.current === narrationLanguage) return;
    previousLanguageRef.current = narrationLanguage;
    if (playingSite && player.currentSiteId === playingSite.siteId) {
      void playSiteAudio(playingSite, true);
    }
  }, [narrationLanguage, playingSite, player.currentSiteId, playSiteAudio]);

  function applyGeofenceUpdate(position: Coordinates, sites: PoiOfflineData[]) {
    const previousState = geofenceRef.current;
    const nextState = updateGeofenceState(position, sites, geofenceRef.current);
    geofenceRef.current = nextState;
    setGeofence(nextState);

    if (nextState.activeSiteId !== null) {
      setSelectedSiteId(nextState.activeSiteId);
      if (
        autoPlayEnabledRef.current &&
        nextState.activeSiteId !== previousState.activeSiteId &&
        canAutoPlay(nextState.activeSiteId)
      ) {
        const activeSite = sites.find((site) => site.siteId === nextState.activeSiteId);
        if (activeSite) void playSiteAudio(activeSite, false);
      }
    }
  }

  useEffect(() => {
    const controller = new AbortController();
    let disposed = false;

    async function loadOfflinePois() {
      let cachedVersion = 0;
      let hasCachedSnapshot = false;
      try {
        const snapshot = await readPoiSnapshot();
        if (snapshot && !disposed) {
          hasCachedSnapshot = true;
          cachedVersion = snapshot.serverDataVersion;
          setDataVersion(snapshot.serverDataVersion);
          poiListRef.current = snapshot.poiList;
          setPoiList(snapshot.poiList);
          setSelectedSiteId((current) => current ?? snapshot.poiList[0]?.siteId ?? null);
          if (userLocationRef.current) {
            applyGeofenceUpdate(userLocationRef.current, snapshot.poiList);
          }
        }
      } catch {
        if (!disposed) setSyncMessage(t("map", "syncReadError"));
      }

      try {
        const synced = await syncPoiData(cachedVersion, controller.signal);
        if (synced && !disposed) {
          const snapshot = {
            serverDataVersion: synced.serverDataVersion,
            poiList: synced.poiList,
          };
          poiListRef.current = snapshot.poiList;
          setPoiList(snapshot.poiList);
          setDataVersion(snapshot.serverDataVersion);
          setSelectedSiteId((current) => current ?? snapshot.poiList[0]?.siteId ?? null);
          if (!hasCachedSnapshot && userLocationRef.current) {
            applyGeofenceUpdate(userLocationRef.current, snapshot.poiList);
          }
          await savePoiSnapshot(snapshot);
        }
        if (!disposed) setSyncMessage(null);
      } catch (error) {
        if (controller.signal.aborted || disposed) return;
        setSyncMessage(
          error instanceof Error
            ? `${error.message} ${cachedVersion > 0 ? t("map", "syncOffline") : ""}`
            : t("map", "syncUnavailable"),
        );
      } finally {
        if (!disposed) setLoadingPois(false);
      }
    }

    void loadOfflinePois();
    return () => {
      disposed = true;
      controller.abort();
    };
  }, []);

  useEffect(() => {
    if (!navigator.geolocation) return;

    const watchId = navigator.geolocation.watchPosition(
      (position) => {
        const currentPosition = {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          accuracy: position.coords.accuracy,
        };
        userLocationRef.current = currentPosition;
        setUserLocation(currentPosition);
        applyGeofenceUpdate(currentPosition, poiListRef.current);
        setLocationMessage(null);
      },
      (error) => {
        const message =
          error.code === error.PERMISSION_DENIED
            ? t("map", "gpsPermission")
            : t("map", "gpsUnavailable");
        setLocationMessage(message);
      },
      { enableHighAccuracy: true, maximumAge: 10_000, timeout: 15_000 },
    );

    return () => navigator.geolocation.clearWatch(watchId);
  }, []);

  useEffect(() => {
    const activeSiteId = geofence.activeSiteId;
    if (activeSiteId === null) return;

    const activeSite = poiListRef.current.find((site) => site.siteId === activeSiteId);
    if (!activeSite) return;

    const controller = new AbortController();
    void getRegionAudioManifest(activeSite.regionId, narrationLanguage, controller.signal).catch(() => {
      // Playback continues from the local POI snapshot when the manifest is unavailable.
    });

    return () => controller.abort();
  }, [geofence.activeSiteId, narrationLanguage]);

  const selectedPoi = poiList.find((poi) => poi.siteId === selectedSiteId) ?? null;
  const displayedGuide = playingSite ?? selectedPoi;
  const selectedTranslation = displayedGuide ? getPreferredTranslation(displayedGuide) : null;
  const handleQrCode = useCallback(
    async (qrCodeValue: string) => {
      try {
        const detail = await getTouristSiteByQr(qrCodeValue);
        const cachedPoi = poiListRef.current.find((poi) => poi.siteId === detail.siteId);
        const site: PoiOfflineData = cachedPoi
          ? {
              ...cachedPoi,
              regionId: detail.regionId,
              name: detail.name,
              description: detail.description,
              address: detail.address,
              imageUrl: detail.imageUrl,
              translations: detail.translations.map((translation) => ({
                languageCode: translation.languageCode,
                audioUrl: translation.audioUrl,
                content: translation.content,
              })),
            }
          : {
              siteId: detail.siteId,
              regionId: detail.regionId,
              name: detail.name,
              address: detail.address,
              description: detail.description,
              latitude: 0,
              longitude: 0,
              triggerRadius: 0,
              imageUrl: detail.imageUrl,
              translations: detail.translations.map((translation) => ({
                languageCode: translation.languageCode,
                audioUrl: translation.audioUrl,
                content: translation.content,
              })),
            };
        await playSiteAudio(site, true);
      } catch {
        const offlineSiteId = Number(qrCodeValue);
        const offlineSite = Number.isInteger(offlineSiteId)
          ? poiListRef.current.find((poi) => poi.siteId === offlineSiteId)
          : undefined;
        if (offlineSite) {
          await playSiteAudio(offlineSite, true);
        } else {
          setSyncMessage(t("qr", "offlineError"));
        }
      }
    },
    [playSiteAudio],
  );
  const handleQrDetected = useCallback(
    (value: string) => {
      setIsQrScannerOpen(false);
      void handleQrCode(value);
    },
    [handleQrCode],
  );
  const mapCenter = (mapFocus === "user" ? userLocation : selectedPoi) ?? userLocation ?? DEFAULT_CENTER;
  const sortedPois = useMemo(
    () =>
      poiList
        .map((poi) => ({
          poi,
          distance: userLocation ? calculateHaversineDistance(userLocation, poi) : null,
        }))
        .sort((left, right) => (left.distance ?? Infinity) - (right.distance ?? Infinity)),
    [poiList, userLocation],
  );

  return (
    <main className="map-page">
      <header className="map-header">
        <div>
          <span className="map-kicker">{t("app", "brand")} · {t("app", "offlineGuide")}</span>
          <h1>{t("map", "title")}</h1>
        </div>
        <div className="map-status">
          <span className={`gps-indicator${userLocation ? " is-live" : ""}`} />
          <span>{userLocation ? t("map", "gpsReceiving") : t("map", "gpsWaiting")}</span>
          <span className="version-label">{t("map", "dataVersion", { version: dataVersion })}</span>
          <button
            className="locate-button"
            type="button"
            disabled={!userLocation}
            aria-pressed={mapFocus === "user"}
            onClick={() => setMapFocus((current) => (current === "user" ? "site" : "user"))}
          >
            {mapFocus === "user" ? t("map", "backToDestination") : t("map", "myLocation")}
          </button>
          <button
            className={`auto-play-button${autoPlayEnabled ? " is-enabled" : ""}`}
            type="button"
            aria-pressed={autoPlayEnabled}
            onClick={() => {
              const nextEnabled = !autoPlayEnabledRef.current;
              autoPlayEnabledRef.current = nextEnabled;
              setAutoPlayEnabled(nextEnabled);
            }}
          >
            {autoPlayEnabled ? t("map", "autoPlayOn") : t("map", "autoPlayOff")}
          </button>
          <LanguageSelector />
          <LanguageSelector mode="narration" />
        </div>
      </header>

      {(locationMessage || syncMessage) && (
        <div className="map-notices" role="status">
          {locationMessage && <p>{locationMessage}</p>}
          {syncMessage && <p>{syncMessage}</p>}
        </div>
      )}

      <div className="map-layout">
        <section className="map-frame" aria-label={t("map", "destinations")}>
          <MapContainer
            center={[mapCenter.latitude, mapCenter.longitude]}
            zoom={14}
            scrollWheelZoom={false}
            className="map-leaflet"
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <FollowMapCenter center={mapCenter} />
            {poiList.map((poi) => {
              const isActive = poi.siteId === geofence.activeSiteId;
              const isSelected = poi.siteId === selectedSiteId;
              const markerColor = isActive ? "#b8732d" : "#247257";

              return (
                <Fragment key={poi.siteId}>
                  <Circle
                    center={[poi.latitude, poi.longitude]}
                    radius={poi.triggerRadius}
                    pathOptions={{
                      color: markerColor,
                      weight: isSelected ? 2 : 1,
                      fillColor: markerColor,
                      fillOpacity: isActive ? 0.18 : 0.07,
                    }}
                  />
                  <CircleMarker
                    center={[poi.latitude, poi.longitude]}
                    radius={isSelected ? 10 : 7}
                    pathOptions={{
                      color: "#fff",
                      weight: 2,
                      fillColor: markerColor,
                      fillOpacity: 1,
                    }}
                    eventHandlers={{
                      click: () => {
                        setSelectedSiteId(poi.siteId);
                        setMapFocus("site");
                      },
                    }}
                  >
                    <Popup>
                      <strong>{poi.name}</strong>
                      {poi.address && <div>{poi.address}</div>}
                      <button
                        className="popup-select-button"
                        type="button"
                        onClick={() => {
                          setSelectedSiteId(poi.siteId);
                          setMapFocus("site");
                        }}
                      >
                        {t("map", "selectDestination")}
                      </button>
                      <button
                        className="popup-listen-button"
                        type="button"
                        onClick={() => void playSiteAudio(poi, true)}
                      >
                        {t("map", "listen")}
                      </button>
                    </Popup>
                  </CircleMarker>
                </Fragment>
              );
            })}
            {userLocation && (
              <>
                <Circle
                  center={[userLocation.latitude, userLocation.longitude]}
                  radius={Math.min(userLocation.accuracy ?? 20, 200)}
                  pathOptions={{ color: "#3978b8", fillColor: "#3978b8", fillOpacity: 0.1, weight: 1 }}
                />
                <CircleMarker
                  center={[userLocation.latitude, userLocation.longitude]}
                  radius={8}
                  pathOptions={{ color: "#fff", weight: 3, fillColor: "#2875c7", fillOpacity: 1 }}
                >
                  <Popup>
                    {t("map", "userHere", { meters: Math.round(userLocation.accuracy ?? 0) })}
                  </Popup>
                </CircleMarker>
              </>
            )}
          </MapContainer>
          {userLocation && (
            <div className="position-readout">
              <span className="position-dot" />
              {t("map", "gpsAccuracy", { meters: Math.round(userLocation.accuracy ?? 0) })}
            </div>
          )}
          <button className="qr-floating-button" type="button" onClick={() => setIsQrScannerOpen(true)}>
            <span aria-hidden="true">⌁</span> {t("map", "qrScan")}
          </button>
        </section>

        <aside className="poi-panel" aria-label={t("map", "destinations")}>
          <div className="poi-panel-heading">
            <div>
              <span className="map-kicker">{t("map", "synced")}</span>
              <h2>{t("map", "destinations")}</h2>
            </div>
            <span className="poi-count">{poiList.length} {t("map", "destinationCount")}</span>
          </div>

          {loadingPois && poiList.length === 0 ? (
            <p className="poi-empty">{t("map", "loadingPois")}</p>
          ) : sortedPois.length === 0 ? (
            <p className="poi-empty">{t("map", "noPois")}</p>
          ) : (
            <div className="poi-list">
              {sortedPois.map(({ poi, distance }) => {
                const isActive = poi.siteId === geofence.activeSiteId;
                const isSelected = poi.siteId === selectedSiteId;

                return (
                  <button
                    className={`poi-row${isSelected ? " is-selected" : ""}${isActive ? " is-active" : ""}`}
                    key={poi.siteId}
                    type="button"
                    onClick={() => {
                      setSelectedSiteId(poi.siteId);
                      setMapFocus("site");
                    }}
                    aria-pressed={isSelected}
                  >
                    {poi.imageUrl ? (
                      <img src={resolveAssetUrl(poi.imageUrl)} alt="" />
                    ) : (
                      <span className="poi-image-fallback" aria-hidden="true">
                        LF
                      </span>
                    )}
                    <span className="poi-row-copy">
                      <strong>{poi.name}</strong>
                      <span>
                        {poi.address ||
                          getPreferredTranslation(poi)?.content ||
                          poi.description ||
                          `${t("map", "destinations")} ${poi.siteId}`}
                      </span>
                      {distance !== null && (
                        <small>{t("map", "distance", { meters: Math.round(distance) })}</small>
                      )}
                    </span>
                    {isActive && <span className="nearby-tag">{t("map", "nearby")}</span>}
                  </button>
                );
              })}
            </div>
          )}
        </aside>
      </div>

      {displayedGuide && (
        <section className="active-guide" aria-live="polite">
          <div>
            <span className="map-kicker">
              {geofence.activeSiteId === displayedGuide.siteId
                ? t("map", "activeGuide")
                : t("map", "selectedGuide")}
            </span>
            <h2>{displayedGuide.name}</h2>
            {selectedTranslation?.content ? (
              <p className="active-guide-description">{selectedTranslation.content}</p>
            ) : displayedGuide.description ? (
              <p className="active-guide-description">{displayedGuide.description}</p>
            ) : null}
          </div>
          {player.currentSiteId === displayedGuide.siteId && player.currentUrl ? (
            <div className="narration-controls">
              <button type="button" onClick={player.isPlaying ? player.pause : () => void player.play(
                displayedGuide.siteId,
                player.currentUrl ?? "",
                true,
              )}>
                {player.isPlaying ? t("audio", "pause") : t("audio", "play")}
              </button>
              <button type="button" onClick={player.replay}>{t("audio", "replay")}</button>
              <span className="narration-time">
                {t("audio", "timeSeconds", {
                  current: Math.floor(player.currentTime),
                  duration: Math.floor(player.duration),
                })}
              </span>
              {player.error && (
                <span className="narration-error">
                  {player.error === "autoplayBlocked"
                    ? t("audio", "autoplayBlocked")
                    : t("audio", "playbackError")}
                </span>
              )}
            </div>
          ) : displayedGuide.translations.length > 0 ? (
            <button type="button" className="listen-button" onClick={() => void playSiteAudio(displayedGuide, true)}>
              {t("map", "listen")}
            </button>
          ) : (
            <span className="poi-empty">{t("map", "noAudio")}</span>
          )}
        </section>
      )}
      <QRScannerModal
        isOpen={isQrScannerOpen}
        onClose={() => setIsQrScannerOpen(false)}
        onDetected={handleQrDetected}
      />
    </main>
  );
}

export default MapPage;
