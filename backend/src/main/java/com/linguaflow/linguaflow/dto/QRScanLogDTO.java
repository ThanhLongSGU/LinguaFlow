package com.linguaflow.linguaflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRScanLogDTO {

    private Long scanId;
    private Integer qrId;
    private LocalDateTime scannedAt;
}