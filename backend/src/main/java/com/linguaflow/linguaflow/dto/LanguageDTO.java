package com.linguaflow.linguaflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDTO {

    private Integer languageId;
    private String code;
    private String name;
    private Boolean active;
}