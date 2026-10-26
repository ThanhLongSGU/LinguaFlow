package com.linguaflow.linguaflow.repository;

import com.linguaflow.linguaflow.entity.SiteLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteLocationRepository extends JpaRepository<SiteLocation, Integer> {
}