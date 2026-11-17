package com.linguaflow.linguaflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "Audio", uniqueConstraints = @UniqueConstraint(
        name = "UQ_Audio_Translation",
        columnNames = "TranslationId"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Audio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AudioId")
    private Long audioId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TranslationId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Translation translation;

    @Column(name = "StorageUrl", nullable = false, length = 1000)
    private String storageUrl;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @Column(name = "ContentHash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "AudioVersion", nullable = false)
    private Integer audioVersion;

    @Column(name = "ErrorMessage", columnDefinition = "NVARCHAR(MAX)")
    private String errorMessage;

    @Column(name = "RetryCount", nullable = false)
    private Integer retryCount;

    @Column(name = "GeneratedAt")
    private LocalDateTime generatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
