package com.linguaflow.linguaflow.repository;

import com.linguaflow.linguaflow.entity.Audio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AudioRepository extends JpaRepository<Audio, Long> {

    @Query("""
            SELECT audio
            FROM Audio audio
            JOIN FETCH audio.translation translation
            JOIN FETCH translation.language language
            JOIN FETCH translation.narration narration
            JOIN FETCH narration.touristSite site
            JOIN FETCH site.region region
            WHERE region.regionId = :regionId
              AND language.code = :languageCode
              AND audio.status = 'READY'
            ORDER BY site.siteId
            """)
    List<Audio> findReadyByRegionAndLanguage(
            @Param("regionId") Integer regionId,
            @Param("languageCode") String languageCode);
}
