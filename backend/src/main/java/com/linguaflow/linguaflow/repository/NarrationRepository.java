package com.linguaflow.linguaflow.repository;

import com.linguaflow.linguaflow.entity.Narration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NarrationRepository extends JpaRepository<Narration, Integer> {
    boolean existsByTouristSite_SiteId(Integer siteId);
}