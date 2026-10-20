package com.linguaflow.linguaflow.repository;

import com.linguaflow.linguaflow.entity.TouristSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TouristSiteRepository extends JpaRepository<TouristSite, Integer> {

    @Query("""
            SELECT DISTINCT site
            FROM TouristSite site
            LEFT JOIN FETCH site.narrations narration
            LEFT JOIN FETCH narration.translations translation
            LEFT JOIN FETCH translation.language
            WHERE site.siteId = :siteId
            """)
    Optional<TouristSite> findByIdWithTranslations(@Param("siteId") Integer siteId);
}