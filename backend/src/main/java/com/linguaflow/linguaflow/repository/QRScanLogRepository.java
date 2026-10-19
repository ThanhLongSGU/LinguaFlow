
    package com.linguaflow.linguaflow.repository;

    import com.linguaflow.linguaflow.entity.QRScanLog;
    import org.springframework.data.jpa.repository.JpaRepository;

    public interface QRScanLogRepository
            extends JpaRepository<QRScanLog, Long> {
    }
