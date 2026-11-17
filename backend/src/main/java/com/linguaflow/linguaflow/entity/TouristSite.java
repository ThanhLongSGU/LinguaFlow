package com.linguaflow.linguaflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "TouristSites")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TouristSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SiteId")
    private Integer siteId;

    @Column(name = "Name", nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CategoryId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RegionId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Region region;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "Address", length = 255)
    private String address;

    @Column(name = "City", length = 100)
    private String city;

    @Column(name = "Latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "Longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Builder.Default
    @Column(name = "TriggerRadius", nullable = false)
    private Integer triggerRadius = 20;

    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;

    @Column(name = "ReferenceTicketPrice", precision = 12, scale = 2)
    private BigDecimal referenceTicketPrice;

    @Column(name = "Status", nullable = false)
    private Boolean status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CreatedBy", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdBy;

    @OneToMany(mappedBy = "touristSite", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<QRCode> qrCodes = new ArrayList<>();

    @OneToMany(mappedBy = "touristSite", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Narration> narrations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "touristSite", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<SiteLanguage> siteLanguages = new LinkedHashSet<>();

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