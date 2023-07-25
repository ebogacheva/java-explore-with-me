package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryServiceImpl;

import java.util.List;

@RestController
@RequestMapping(path = "/categories")
@RequiredArgsConstructor
public class CategoryPublicController {

    private final CategoryServiceImpl categoryService;

    @GetMapping
    public List<CategoryDto> get(@RequestParam(required = false, defaultValue = "0") Integer from,
                                 @RequestParam(required = false, defaultValue = "10") Integer size) {
        return categoryService.get(from, size);
    }

    @GetMapping(value = "/{catId}")
    public CategoryDto get(@PathVariable Long catId){
        return categoryService.get(catId);
    }
}