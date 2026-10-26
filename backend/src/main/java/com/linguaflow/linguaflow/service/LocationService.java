package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.NearbySiteDTO;
import com.linguaflow.linguaflow.entity.SiteLocation;
import com.linguaflow.linguaflow.entity.TouristSite;
import com.linguaflow.linguaflow.repository.SiteLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final SiteLocationRepository siteLocationRepository;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public NearbySiteDTO findNearestSite(double userLat, double userLng) {
        List<SiteLocation> locations = siteLocationRepository.findAll();

        NearbySiteDTO nearestSite = null;
        double minDistanceMeters = Double.MAX_VALUE;

        for (SiteLocation loc : locations) {
            TouristSite site = loc.getTouristSite();
            // Khắc phục Lỗi 1: Kiểm tra trạng thái hoạt động theo kiểu Boolean thay vì so sánh số nguyên int
            if (site == null || Boolean.FALSE.equals(site.getStatus())) continue;

            double distanceKm = calculateHaversineDistance(userLat, userLng, loc.getLatitude(), loc.getLongitude());
            double distanceMeters = distanceKm * 1000.0;

            if (distanceMeters <= loc.getRadius() && distanceMeters < minDistanceMeters) {
                minDistanceMeters = distanceMeters;

                // Khắc phục Lỗi 2: Sử dụng đúng phương thức .distance() của NearbySiteDTOBuilder
                nearestSite = NearbySiteDTO.builder()
                        .siteId(site.getSiteId())
                        .name(site.getName())
                        .address(site.getAddress())
                        .imageUrl(site.getImageUrl())
                        .distance(Math.round(distanceMeters * 10.0) / 10.0)
                        .build();
            }
        }
        return nearestSite;
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));

        return EARTH_RADIUS_KM * c;
    }
}