package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.EWMElementNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.utils.EWMCommonMethods.pageRequestOf;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE = "Категория не найдена.";

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Override
    public CategoryDto create(NewCategoryDto category) {
        Category newCategory = mapper.newCategoryDtoToCategory(category);
        Category saved = repository.save(newCategory);
        return mapper.categoryToCategoryDto(saved);
    }

    @Override
    public void delete(Long catId) {
        getCategoryIfExists(catId);
        repository.deleteById(catId);
    }

    @Override
    public CategoryDto update(CategoryDto categoryDto, Long catId) {
        Category category = getCategoryIfExists(catId);
        updateCategoryByDto(category, categoryDto);
        return mapper.categoryToCategoryDto(repository.save(category));
    }

    @Override
    public List<CategoryDto> get(Integer from, Integer size) {
        Pageable pageable = pageRequestOf(from, size);
        Page<Category> categories = repository.findAll(pageable);
        return mapper.pageToList(categories)
                .stream()
                .map(mapper::categoryToCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto get(Long catId) {
        Category category = getCategoryIfExists(catId);
        return mapper.categoryToCategoryDto(category);
    }

    private Category getCategoryIfExists(Long catId) {
        return repository.findById(catId)
                .orElseThrow(() -> new EWMElementNotFoundException(CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private void updateCategoryByDto(Category category, CategoryDto categoryDto) {
        category.setName(categoryDto.getName());
    }
}
