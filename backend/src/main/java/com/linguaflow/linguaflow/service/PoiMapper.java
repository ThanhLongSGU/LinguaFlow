package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.PoiSyncResponse;
import com.linguaflow.linguaflow.entity.TouristSite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.LinkedHashMap;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PoiMapper {

        @Mapping(source = "category.categoryId", target = "categoryId")
        @Mapping(target = "translations", ignore = true)
        PoiSyncResponse.PoiItem toPoiItemBase(TouristSite site);

        default PoiSyncResponse.PoiItem toPoiItem(TouristSite site) {
                var base = toPoiItemBase(site);
                var translationsByLanguage = new LinkedHashMap<String, PoiSyncResponse.PoiTranslation>();

                site.getNarrations().stream()
                                .flatMap(narration -> narration.getTranslations().stream())
                                .forEach(translation -> translationsByLanguage.putIfAbsent(
                                                translation.getLanguage().getCode(),
                                                new PoiSyncResponse.PoiTranslation(
                                                                translation.getLanguage().getCode(),
                                                                translation.getAudioUrl())));

                return new PoiSyncResponse.PoiItem(
                                base.siteId(),
                                base.name(),
                                base.categoryId(),
                                base.description(),
                                base.latitude(),
                                base.longitude(),
                                base.triggerRadius(),
                                base.imageUrl(),
                                List.copyOf(translationsByLanguage.values()));
        }
}