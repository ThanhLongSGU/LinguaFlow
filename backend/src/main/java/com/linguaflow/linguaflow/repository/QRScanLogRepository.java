package com.linguaflow.linguaflow.repository;

import com.linguaflow.linguaflow.entity.QRScanLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QRScanLogRepository extends JpaRepository<QRScanLog, Long> {

    Page<QRScanLog> findByQrId(Integer qrId, Pageable pageable);
}