package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.TranslationDTO;
import com.linguaflow.linguaflow.dto.TouristSiteDetailResponse;
import com.linguaflow.linguaflow.dto.TouristSiteDTO;
import com.linguaflow.linguaflow.entity.Narration;
import com.linguaflow.linguaflow.entity.TouristSite;
import com.linguaflow.linguaflow.entity.Translation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TouristSiteMapper {

    TouristSiteDTO toDto(TouristSite touristSite);

    @Mapping(target = "translations", ignore = true)
    TouristSiteDetailResponse toBaseDetailResponse(TouristSite touristSite);

    default TouristSiteDetailResponse toDetailResponse(TouristSite touristSite) {
        TouristSiteDetailResponse response = toBaseDetailResponse(touristSite);
        response.setTranslations(touristSite.getNarrations().stream()
                .flatMap(narration -> narration.getTranslations().stream())
                .map(this::toTranslationDto)
                .toList());
        return response;
    }

    default TranslationDTO toTranslationDto(Translation translation) {
        return TranslationDTO.builder()
                .translationId(translation.getTranslationId())
                .narrationId(translation.getNarration().getNarrationId())
                .languageId(translation.getLanguage().getLanguageId())
                .languageCode(translation.getLanguage().getCode())
                .languageName(translation.getLanguage().getName())
                .content(translation.getContent())
                .audioUrl(translation.getAudioUrl())
                .build();
    }
}