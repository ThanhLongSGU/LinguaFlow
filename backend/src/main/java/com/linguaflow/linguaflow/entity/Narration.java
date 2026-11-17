package com.linguaflow.linguaflow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Narrations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Narration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NarrationId")
    private Integer narrationId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SiteId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TouristSite touristSite;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "OriginalContent", columnDefinition = "NVARCHAR(MAX)")
    private String originalContent;

    @Column(name = "ContentHash", columnDefinition = "VARCHAR(64)")
    private String contentHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "narration", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Translation> translations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "narration", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<AiTranslationTask> aiTranslationTasks = new ArrayList<>();

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}