package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.CreateTouristSiteRequest;
import com.linguaflow.linguaflow.dto.TouristSiteDetailResponse;
import com.linguaflow.linguaflow.dto.TouristSiteDTO;
import com.linguaflow.linguaflow.service.TouristSiteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping
    public ResponseEntity<TouristSiteDTO> createTouristSite(
            @Valid @RequestBody CreateTouristSiteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(touristSiteService.createTouristSite(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TouristSiteDTO> updateTouristSite(
            @PathVariable Integer id,
            @Valid @RequestBody CreateTouristSiteRequest request) {
        return ResponseEntity.ok(touristSiteService.updateTouristSite(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTouristSite(@PathVariable Integer id) {
        touristSiteService.deleteTouristSite(id);
        return ResponseEntity.noContent().build();
    }
}