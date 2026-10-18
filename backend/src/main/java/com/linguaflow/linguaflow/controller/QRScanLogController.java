package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.QRScanLogDTO;
import com.linguaflow.linguaflow.service.QRScanLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/scan-logs")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class QRScanLogController {

    private final QRScanLogService qrScanLogService;


    @PostMapping
    public ResponseEntity<QRScanLogDTO> recordScanLog(@RequestBody QRScanLogDTO scanLogDTO) {
        QRScanLogDTO createdLog = qrScanLogService.recordScanLog(scanLogDTO.getQrId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLog);
    }

    @GetMapping
    public ResponseEntity<Page<QRScanLogDTO>> getScanLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<QRScanLogDTO> scanLogs = qrScanLogService.getScanLogs(page, size);
        return ResponseEntity.ok(scanLogs);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        Map<String, Object> analytics = qrScanLogService.getAnalyticsData();
        return ResponseEntity.ok(analytics);
    }
}