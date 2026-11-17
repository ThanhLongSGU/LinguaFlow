package com.linguaflow.linguaflow.repository;

import com.linguaflow.linguaflow.entity.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QRCodeRepository extends JpaRepository<QRCode, Integer> {

    @Query("SELECT q FROM QRCode q JOIN FETCH q.touristSite WHERE q.qrCodeValue = :qrCodeValue")
    Optional<QRCode> findByQrCodeValue(@Param("qrCodeValue") String qrCodeValue);

    boolean existsByQrCodeValue(String qrCodeValue);

    boolean existsByTouristSite_SiteId(Integer siteId);
}