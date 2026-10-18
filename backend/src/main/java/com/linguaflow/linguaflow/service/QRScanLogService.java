package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.QRScanLogDTO;
import com.linguaflow.linguaflow.entity.QRScanLog;
import com.linguaflow.linguaflow.repository.QRScanLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QRScanLogService {

    private final QRScanLogRepository scanLogRepository;

    @Transactional
    public QRScanLogDTO recordScanLog(Integer qrId) {
        QRScanLog scanLog = new QRScanLog();
        scanLog.setQrId(qrId);
        scanLog.setScannedAt(LocalDateTime.now());

        QRScanLog savedLog = scanLogRepository.save(scanLog);

        return QRScanLogDTO.builder()
                .scanId(savedLog.getScanId())
                .qrId(savedLog.getQrId())
                .scannedAt(savedLog.getScannedAt())
                .build();
    }

    public Page<QRScanLogDTO> getScanLogs(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return scanLogRepository.findAll(pageable)
                .map(log -> QRScanLogDTO.builder()
                        .scanId(log.getScanId())
                        .qrId(log.getQrId())
                        .scannedAt(log.getScannedAt())
                        .build());
    }

    public Map<String, Object> getAnalyticsData() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalScans", scanLogRepository.count());
        return analytics;
    }
}