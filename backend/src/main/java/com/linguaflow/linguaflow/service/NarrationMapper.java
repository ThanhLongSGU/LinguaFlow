package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.NarrationDTO;
import com.linguaflow.linguaflow.entity.Narration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NarrationMapper {

    @Mapping(source = "touristSite.siteId", target = "siteId")
    NarrationDTO toDto(Narration narration);
}