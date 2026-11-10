package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.PoiSyncResponse;
import com.linguaflow.linguaflow.repository.PoiSyncMetadataRepository;
import com.linguaflow.linguaflow.repository.TouristSiteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PoiService {

    private final PoiSyncMetadataRepository poiSyncMetadataRepository;
    private final TouristSiteRepository touristSiteRepository;
    private final PoiMapper poiMapper;

    @Transactional(readOnly = true)
    public long getCurrentDataVersion() {
        return poiSyncMetadataRepository.findById(1)
                .map(metadata -> metadata.getDataVersion())
                .orElseThrow(() -> new EntityNotFoundException("POI sync metadata is not initialized"));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "poi_sync", key = "#serverDataVersion")
    public PoiSyncResponse getPoiSnapshot(long serverDataVersion) {
        var poiList = touristSiteRepository.findActiveSitesWithPoiTranslations()
                .stream()
                .map(poiMapper::toPoiItem)
                .toList();

        return new PoiSyncResponse(serverDataVersion, true, poiList);
    }
}