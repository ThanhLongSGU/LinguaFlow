package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.CategoryDTO;
import com.linguaflow.linguaflow.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDTO toDto(Category category);

    @Mapping(target = "touristSites", ignore = true)
    Category toEntity(CategoryDTO categoryDTO);
}