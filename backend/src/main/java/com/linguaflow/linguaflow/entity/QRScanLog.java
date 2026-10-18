package com.linguaflow.linguaflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "QRScanLogs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRScanLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ScanId")
    private Long scanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QrId", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private QRCode qrCode;

    @Column(name = "QrId", nullable = false)
    private Integer qrId;

    @Column(name = "ScannedAt", nullable = false)
    @Builder.Default
    private LocalDateTime scannedAt = LocalDateTime.now();

    public Integer getQrId() {
        if (this.qrId != null) {
            return this.qrId;
        }
        return this.qrCode != null ? this.qrCode.getQrId() : null;
    }

    public void setQrId(Integer qrId) {
        this.qrId = qrId;
    }
}