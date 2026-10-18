package com.linguaflow.linguaflow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Languages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LanguageId")
    private Integer languageId;

    @Column(name = "Code", nullable = false, unique = true, length = 10, columnDefinition = "VARCHAR(10)")
    private String code;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "IsActive", nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "language", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Translation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "language", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<AiTranslationTask> aiTranslationTasks = new ArrayList<>();
}