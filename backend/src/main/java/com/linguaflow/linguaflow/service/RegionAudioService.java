package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.RegionAudioManifestResponse;
import com.linguaflow.linguaflow.entity.Audio;
import com.linguaflow.linguaflow.repository.AudioRepository;
import com.linguaflow.linguaflow.repository.RegionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RegionAudioService {

    private final AudioRepository audioRepository;
    private final RegionRepository regionRepository;

    @Transactional(readOnly = true)
    public RegionAudioManifestResponse getManifest(Integer regionId, String languageCode) {
        regionRepository.findById(regionId)
                .filter(region -> Boolean.TRUE.equals(region.getActive()))
                .orElseThrow(() -> new EntityNotFoundException("Region not found with id: " + regionId));

        var items = audioRepository.findReadyByRegionAndLanguage(regionId, languageCode).stream()
                .filter(this::hasCurrentContentHash)
                .map(audio -> new RegionAudioManifestResponse.AudioItem(
                        audio.getAudioId(),
                        audio.getTranslation().getNarration().getTouristSite().getSiteId(),
                        audio.getTranslation().getTranslationId(),
                        audio.getTranslation().getLanguage().getCode(),
                        audio.getAudioVersion(),
                        audio.getContentHash(),
                        audio.getStorageUrl()))
                .toList();

        return new RegionAudioManifestResponse(regionId, languageCode, Instant.now(), items);
    }

    private boolean hasCurrentContentHash(Audio audio) {
        String content = audio.getTranslation().getContent();
        return content != null
                && audio.getContentHash() != null
                && audio.getContentHash().equals(sha256(content));
    }

    private String sha256(String content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}
