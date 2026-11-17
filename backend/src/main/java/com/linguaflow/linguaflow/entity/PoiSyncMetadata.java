package com.linguaflow.linguaflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PoiSyncMetadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoiSyncMetadata {

    @Id
    @Column(name = "MetadataId")
    private Integer metadataId;

    @Column(name = "DataVersion", nullable = false)
    private Long dataVersion;
}