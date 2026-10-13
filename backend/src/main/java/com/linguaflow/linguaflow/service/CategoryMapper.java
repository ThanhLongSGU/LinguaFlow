package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.CategoryDTO;
import com.linguaflow.linguaflow.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDTO toDto(Category category);

    Category toEntity(CategoryDTO categoryDTO);
}