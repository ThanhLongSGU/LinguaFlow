import { useEffect } from 'react'
import { MapContainer, TileLayer, CircleMarker, Popup, Circle, useMap } from 'react-leaflet'

function FollowPosition({ position, enabled }) {
  const map = useMap()
  useEffect(() => {
    if (position && enabled) map.panTo([position.latitude, position.longitude])
  }, [map, position?.latitude, position?.longitude, enabled])
  return null
}

export default function MapPanel({ sites = [], position, currentSiteId,
                                  counts = {}, height = 410, follow = true }) {
  return (
    <div className="map-shell" style={{ height }}>
      <MapContainer center={[10.779, 106.698]} zoom={15} scrollWheelZoom={false}
                    style={{ height: '100%', width: '100%' }}>
        <TileLayer attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                   url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        {sites.map(site => (
          <ReactFragmentMarker key={site.siteId} site={site} active={site.siteId === currentSiteId}
                               count={counts[site.siteId]} />
        ))}
        {position && <>
          <CircleMarker center={[position.latitude, position.longitude]} radius={9}
                        pathOptions={{ color: '#fff', weight: 3, fillColor: '#2861e4', fillOpacity: 1 }}>
            <Popup>Vị trí của em (độ chính xác ±{Math.round(position.accuracy)}m)</Popup>
          </CircleMarker>
          <Circle center={[position.latitude, position.longitude]} radius={Math.min(position.accuracy || 10, 200)}
                  pathOptions={{ color: '#2861e4', fillColor: '#2861e4', fillOpacity: 0.07, weight: 1 }} />
        </>}
        <FollowPosition position={position} enabled={follow} />
      </MapContainer>
    </div>
  )
}
function ReactFragmentMarker({ site, active, count }) {
  return <>
    <Circle center={[site.latitude,site.longitude]} radius={site.radiusMeters}
            pathOptions={{ color: active ? '#db8d28' : '#0e8675', weight: active ? 2 : 1,
                           fillOpacity: active ? .14 : .055 }} />
    <CircleMarker center={[site.latitude, site.longitude]} radius={active ? 11 : 8}
                  pathOptions={{ color: '#fff', weight: 2, fillColor: active ? '#d17d16' : '#0f806a', fillOpacity: 1 }}>
      <Popup><strong>{site.name}</strong><br />{site.address || ''}
        {count !== undefined && <><br />Đang hoạt động: {count}</>}</Popup>
    </CircleMarker>
  </>
}
