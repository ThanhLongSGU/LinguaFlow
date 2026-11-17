package com.linguaflow.linguaflow.dto;

import java.time.Instant;
import java.util.List;

public record RegionAudioManifestResponse(
        Integer regionId,
        String languageCode,
        Instant generatedAt,
        List<AudioItem> items) {

    public record AudioItem(
            Long audioId,
            Integer siteId,
            Integer translationId,
            String languageCode,
            Integer audioVersion,
            String contentHash,
            String audioUrl) {
    }
}
