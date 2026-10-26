package com.linguaflow.linguaflow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SiteLocations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LocationId")
    private Integer locationId;

    @Column(name = "SiteId", nullable = false, unique = true)
    private Integer siteId;

    @Column(name = "Latitude", nullable = false)
    private Double latitude;

    @Column(name = "Longitude", nullable = false)
    private Double longitude;

    @Column(name = "Radius", nullable = false)
    @Builder.Default
    private Integer radius = 50;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SiteId", referencedColumnName = "SiteId", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TouristSite touristSite;
}