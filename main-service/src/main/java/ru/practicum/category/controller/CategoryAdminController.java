package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryServiceImpl;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryServiceImpl categoryService;

    @PostMapping
    CategoryDto create(@Valid @RequestBody NewCategoryDto category) {
        return categoryService.create(category);
    }

    @PatchMapping(value = "/{catId}")
    CategoryDto update(@PathVariable Long catId,
                       @Valid @RequestBody CategoryDto category) {
        return categoryService.update(category, catId);
    }

    @DeleteMapping(value = "/{catId}")
    void delete(@PathVariable Long catId) {
        categoryService.delete(catId);
    }
}
