const radians = value => value * Math.PI / 180

export function distanceMeters(a, b) {
  const dLat = radians(b.latitude - a.latitude)
  const dLon = radians(b.longitude - a.longitude)
  const lat1 = radians(a.latitude)
  const lat2 = radians(b.latitude)
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) ** 2
  return 6371000 * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h))
}

// GPS geofencing in the browser. The backend receives only the selected SiteId.
// Two consecutive fixes are required to ENTER/SWITCH sites.
// Hysteresis avoids bouncing between nearby/overlapping tourist sites.
export function identifySite(position, sites, currentSite, candidate) {
  if (!position || position.accuracy > 150) return { site: currentSite, candidate }
  const distances = sites.map(site => ({
    site,
    meters: distanceMeters(position, site),
    ratio: distanceMeters(position, site) / site.radiusMeters
  }))
  const nearest = distances.filter(x => x.ratio <= 1).sort((a, b) => a.ratio - b.ratio)[0]
  const current = currentSite ? distances.find(x => x.site.siteId === currentSite.siteId) : null

  let target = nearest?.site || null
  if (current && current.meters <= current.site.radiusMeters + 40) {
    // Keep current unless the other site is significantly closer.
    if (!nearest || nearest.site.siteId === current.site.siteId ||
        nearest.ratio + 0.30 >= current.ratio) {
      return { site: current.site, candidate: null }
    }
    target = nearest.site
  }
  if (!target) return { site: null, candidate: null }
  if (currentSite?.siteId === target.siteId) return { site: target, candidate: null }
  if (candidate?.siteId === target.siteId) return { site: target, candidate: null }
  return { site: currentSite, candidate: { siteId: target.siteId } }
}
