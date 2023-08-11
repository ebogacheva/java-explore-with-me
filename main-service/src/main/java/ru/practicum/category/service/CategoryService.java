package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(NewCategoryDto category);

    void delete(Long catId);

    CategoryDto update(CategoryDto category, Long catId);

    List<CategoryDto> get(Integer from, Integer size);

    CategoryDto get(Long catId);
}
