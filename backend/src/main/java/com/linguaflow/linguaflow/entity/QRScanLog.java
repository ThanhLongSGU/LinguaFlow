package com.linguaflow.linguaflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "QrId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private QRCode qrCode;

    @Column(name = "ScannedAt", nullable = false)
    private LocalDateTime scannedAt;
}