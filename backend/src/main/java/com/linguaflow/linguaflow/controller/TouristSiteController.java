package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.TouristSiteDetailResponse;
import com.linguaflow.linguaflow.service.TouristSiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tourist-sites")
@RequiredArgsConstructor
public class TouristSiteController {

    private final TouristSiteService touristSiteService;

    @GetMapping("/qr/{qrCodeValue}")
    public ResponseEntity<TouristSiteDetailResponse> getSiteDetailByQrCode(
            @PathVariable String qrCodeValue) {
        return ResponseEntity.ok(touristSiteService.getSiteDetailByQrCode(qrCodeValue));
    }
}