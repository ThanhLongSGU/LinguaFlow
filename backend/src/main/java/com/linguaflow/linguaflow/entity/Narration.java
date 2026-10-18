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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
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

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "narration", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Translation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "narration", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<AiTranslationTask> aiTranslationTasks = new ArrayList<>();
}