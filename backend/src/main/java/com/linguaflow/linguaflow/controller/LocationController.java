package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.NearbySiteDTO;
import com.linguaflow.linguaflow.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/locations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbySite(@RequestParam double lat, @RequestParam double lng) {
        NearbySiteDTO nearestSite = locationService.findNearestSite(lat, lng);
        if (nearestSite != null) {
            return ResponseEntity.ok(nearestSite);
        } else {
            return ResponseEntity.status(404).body("Bạn chưa tiến vào khu vực phát thuyết minh tự động.");
        }
    }
}