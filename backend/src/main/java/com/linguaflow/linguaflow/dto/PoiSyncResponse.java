package com.linguaflow.linguaflow.dto;

import java.math.BigDecimal;
import java.util.List;

public record PoiSyncResponse(
                long serverDataVersion,
                boolean isUpdated,
                List<PoiItem> poiList) {

        public record PoiItem(
                        Integer siteId,
                        String name,
                        Integer categoryId,
                        String description,
                        BigDecimal latitude,
                        BigDecimal longitude,
                        Integer triggerRadius,
                        String imageUrl,
                        List<PoiTranslation> translations) {
        }

        public record PoiTranslation(String languageCode, String audioUrl) {
        }
}