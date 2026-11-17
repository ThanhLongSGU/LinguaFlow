package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.RegionAudioManifestResponse;
import com.linguaflow.linguaflow.service.RegionAudioService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/regions")
public class RegionAudioController {

    private final RegionAudioService regionAudioService;

    @GetMapping("/{regionId}/audio")
    public ResponseEntity<RegionAudioManifestResponse> getAudioManifest(
            @PathVariable Integer regionId,
            @RequestParam @NotBlank @Size(max = 10) String languageCode) {
        return ResponseEntity.ok(regionAudioService.getManifest(regionId, languageCode));
    }
}
