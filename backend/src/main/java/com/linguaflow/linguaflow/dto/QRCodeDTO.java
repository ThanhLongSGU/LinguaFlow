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
public class QRCodeDTO {

    private Integer qrId;
    private Integer siteId;
    private String qrCodeValue;
    private Boolean active;
    private LocalDateTime createdAt;
}