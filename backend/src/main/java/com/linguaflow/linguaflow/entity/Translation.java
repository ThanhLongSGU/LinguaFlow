package com.linguaflow.linguaflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "Translations", uniqueConstraints = @UniqueConstraint(name = "UQ_Translations_Narration_Language", columnNames = {
        "NarrationId", "LanguageId" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TranslationId")
    private Integer translationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NarrationId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Narration narration;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "LanguageId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Language language;

    @Column(name = "Content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "AudioUrl", length = 500)
    private String audioUrl;

    @Column(name = "SourceType", nullable = false, length = 20)
    @Builder.Default
    private String sourceType = "MANUAL";

    @Column(name = "ReviewStatus", nullable = false, length = 20)
    @Builder.Default
    private String reviewStatus = "NOT_REQUIRED";

    @Column(name = "ContentStatus", nullable = false, length = 20)
    @Builder.Default
    private String contentStatus = "AVAILABLE";

    @Column(name = "ContentHash", length = 64)
    private String contentHash;

    @Column(name = "ContentVersion", nullable = false)
    @Builder.Default
    private Integer contentVersion = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReviewedBy")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User reviewedBy;

    @Column(name = "ReviewedAt")
    private LocalDateTime reviewedAt;

    @OneToOne(mappedBy = "translation", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Audio audio;

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
        if (contentVersion == null) {
            contentVersion = 1;
        }
        if (sourceType == null) {
            sourceType = "MANUAL";
        }
        if (reviewStatus == null) {
            reviewStatus = "NOT_REQUIRED";
        }
        if (contentStatus == null) {
            contentStatus = "AVAILABLE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}