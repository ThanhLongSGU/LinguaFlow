package com.linguaflow.linguaflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationDTO {

    private Integer translationId;
    private Integer narrationId;
    private Integer languageId;
    private String languageCode;
    private String languageName;
    private String content;
    private String audioUrl;
}