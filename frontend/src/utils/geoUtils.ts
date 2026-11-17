export interface Coordinates {
  latitude: number;
  longitude: number;
  accuracy?: number;
}

export function calculateHaversineDistance(coord1: Coordinates, coord2: Coordinates): number {
  const radians = (degrees: number) => (degrees * Math.PI) / 180;
  const latitude1 = radians(coord1.latitude);
  const latitude2 = radians(coord2.latitude);
  const latitudeDelta = radians(coord2.latitude - coord1.latitude);
  const longitudeDelta = radians(coord2.longitude - coord1.longitude);
  const rawHaversine =
    Math.sin(latitudeDelta / 2) ** 2 + Math.cos(latitude1) * Math.cos(latitude2) * Math.sin(longitudeDelta / 2) ** 2;
  const haversine = Math.min(1, Math.max(0, rawHaversine));
  const centralAngle = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));

  return 6_371_000 * centralAngle;
}

export interface GeofenceSite extends Coordinates {
  siteId: number;
  triggerRadius: number;
}

export interface GeofenceState {
  activeSiteId: number | null;
  candidateSiteId: number | null;
}

const MAX_ACCEPTED_ACCURACY_METERS = 150;
const EXIT_HYSTERESIS_METERS = 40;
const SITE_SWITCH_RATIO_MARGIN = 0.3;

export function updateGeofenceState(position: Coordinates, sites: GeofenceSite[], state: GeofenceState): GeofenceState {
  if (position.accuracy !== undefined && position.accuracy > MAX_ACCEPTED_ACCURACY_METERS) {
    return state;
  }

  const distances = sites.map((site) => {
    const distanceMeters = calculateHaversineDistance(position, site);
    const triggerRadius = Math.max(site.triggerRadius, 1);
    return { site, distanceMeters, radiusRatio: distanceMeters / triggerRadius };
  });
  const current = distances.find(({ site }) => site.siteId === state.activeSiteId);
  const nearest = distances
    .filter(({ distanceMeters, site }) => distanceMeters <= site.triggerRadius)
    .sort((left, right) => left.radiusRatio - right.radiusRatio)[0];

  let target = nearest?.site ?? null;
  if (current && current.distanceMeters <= current.site.triggerRadius + EXIT_HYSTERESIS_METERS) {
    if (
      !nearest ||
      nearest.site.siteId === current.site.siteId ||
      nearest.radiusRatio + SITE_SWITCH_RATIO_MARGIN >= current.radiusRatio
    ) {
      return { activeSiteId: current.site.siteId, candidateSiteId: null };
    }
    target = nearest.site;
  }

  if (!target) return { activeSiteId: null, candidateSiteId: null };
  if (target.siteId === state.activeSiteId) {
    return { activeSiteId: target.siteId, candidateSiteId: null };
  }
  if (target.siteId === state.candidateSiteId) {
    return { activeSiteId: target.siteId, candidateSiteId: null };
  }

  return { activeSiteId: state.activeSiteId, candidateSiteId: target.siteId };
}
