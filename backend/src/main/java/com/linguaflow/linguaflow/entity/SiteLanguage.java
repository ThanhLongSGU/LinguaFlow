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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "SiteLanguages", uniqueConstraints = @UniqueConstraint(
        name = "UQ_SiteLanguages_Site_Language",
        columnNames = {"SiteId", "LanguageId"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SiteLanguageId")
    private Integer siteLanguageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SiteId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TouristSite touristSite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "LanguageId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Language language;

    @Column(name = "IsEnabled", nullable = false)
    private Boolean enabled;

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
