import { Fragment, useEffect, useMemo, useRef, useState } from "react";
import { Circle, CircleMarker, MapContainer, Popup, TileLayer, useMap } from "react-leaflet";
import { resolveAssetUrl, syncPoiData, type PoiOfflineData } from "../services/api";
import { readPoiSnapshot, savePoiSnapshot } from "../services/poiOfflineStore";
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
  const [poiList, setPoiList] = useState<PoiOfflineData[]>([]);
  const [dataVersion, setDataVersion] = useState(0);
  const [selectedSiteId, setSelectedSiteId] = useState<number | null>(null);
  const [userLocation, setUserLocation] = useState<Coordinates | null>(null);
  const [mapFocus, setMapFocus] = useState<"site" | "user">("site");
  const [geofence, setGeofence] = useState<GeofenceState>(EMPTY_GEOFENCE);
  const [loadingPois, setLoadingPois] = useState(true);
  const [syncMessage, setSyncMessage] = useState<string | null>(null);
  const [locationMessage, setLocationMessage] = useState<string | null>(() =>
    typeof navigator !== "undefined" && !navigator.geolocation ? "Trình duyệt không hỗ trợ định vị GPS." : null,
  );
  const geofenceRef = useRef<GeofenceState>(EMPTY_GEOFENCE);
  const poiListRef = useRef<PoiOfflineData[]>([]);
  const userLocationRef = useRef<Coordinates | null>(null);

  function applyGeofenceUpdate(position: Coordinates, sites: PoiOfflineData[]) {
    const nextState = updateGeofenceState(position, sites, geofenceRef.current);
    geofenceRef.current = nextState;
    setGeofence(nextState);

    if (nextState.activeSiteId !== null) {
      setSelectedSiteId(nextState.activeSiteId);
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
        if (!disposed) setSyncMessage("Không đọc được gói POI đã lưu trên thiết bị.");
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
            ? `${error.message} ${cachedVersion > 0 ? "Đang dùng dữ liệu offline." : ""}`
            : "Không thể đồng bộ POI.",
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
            ? "Hãy cho phép truy cập vị trí để bật thuyết minh theo GPS."
            : "Chưa lấy được vị trí GPS. Bạn vẫn có thể xem bản đồ và các điểm đến.";
        setLocationMessage(message);
      },
      { enableHighAccuracy: true, maximumAge: 10_000, timeout: 15_000 },
    );

    return () => navigator.geolocation.clearWatch(watchId);
  }, []);

  const selectedPoi = poiList.find((poi) => poi.siteId === selectedSiteId) ?? null;
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
          <span className="map-kicker">LINGUAFLOW · OFFLINE GUIDE</span>
          <h1>Khám phá quanh bạn</h1>
        </div>
        <div className="map-status">
          <span className={`gps-indicator${userLocation ? " is-live" : ""}`} />
          <span>{userLocation ? "Đang nhận GPS" : "Đang chờ GPS"}</span>
          <span className="version-label">POI v{dataVersion}</span>
          <button
            className="locate-button"
            type="button"
            disabled={!userLocation}
            aria-pressed={mapFocus === "user"}
            onClick={() => setMapFocus((current) => (current === "user" ? "site" : "user"))}
          >
            {mapFocus === "user" ? "Về điểm đến" : "Vị trí của tôi"}
          </button>
        </div>
      </header>

      {(locationMessage || syncMessage) && (
        <div className="map-notices" role="status">
          {locationMessage && <p>{locationMessage}</p>}
          {syncMessage && <p>{syncMessage}</p>}
        </div>
      )}

      <div className="map-layout">
        <section className="map-frame" aria-label="Bản đồ điểm đến">
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
                        Chọn điểm này
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
                  <Popup>Bạn đang ở đây (sai số ±{Math.round(userLocation.accuracy ?? 0)} m)</Popup>
                </CircleMarker>
              </>
            )}
          </MapContainer>
          {userLocation && (
            <div className="position-readout">
              <span className="position-dot" />
              Sai số GPS khoảng {Math.round(userLocation.accuracy ?? 0)} m
            </div>
          )}
        </section>

        <aside className="poi-panel" aria-label="Danh sách điểm đến">
          <div className="poi-panel-heading">
            <div>
              <span className="map-kicker">ĐÃ ĐỒNG BỘ</span>
              <h2>Điểm đến</h2>
            </div>
            <span className="poi-count">{poiList.length}</span>
          </div>

          {loadingPois && poiList.length === 0 ? (
            <p className="poi-empty">Đang tải gói địa điểm...</p>
          ) : sortedPois.length === 0 ? (
            <p className="poi-empty">Chưa có gói POI trên thiết bị. Kết nối mạng để đồng bộ.</p>
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
                      <span>{poi.address || poi.description || `Địa điểm ${poi.siteId}`}</span>
                      {distance !== null && <small>{Math.round(distance)} m từ vị trí của bạn</small>}
                    </span>
                    {isActive && <span className="nearby-tag">ĐANG Ở ĐÂY</span>}
                  </button>
                );
              })}
            </div>
          )}
        </aside>
      </div>

      {geofence.activeSiteId !== null && selectedPoi && (
        <section className="active-guide" aria-live="polite">
          <div>
            <span className="map-kicker">ĐÃ VÀO VÙNG THUYẾT MINH</span>
            <h2>{selectedPoi.name}</h2>
          </div>
          {selectedPoi.translations.length > 0 ? (
            <audio
              controls
              preload="none"
              src={resolveAssetUrl(
                selectedPoi.translations.find((translation) => translation.languageCode === "vi")?.audioUrl ??
                  selectedPoi.translations[0].audioUrl,
              )}
            >
              Trình duyệt không hỗ trợ phát audio.
            </audio>
          ) : (
            <span className="poi-empty">Chưa có audio cho địa điểm này.</span>
          )}
        </section>
      )}
    </main>
  );
}

export default MapPage;
