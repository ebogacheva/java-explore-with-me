package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.EWMConflictException;
import ru.practicum.exception.EWMElementNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.utils.EWMCommonMethods.pageRequestOf;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private static final String CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE = "Категория не найдена.";

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper mapper;

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto category) {
        Category newCategory = mapper.newCategoryDtoToCategory(category);
        checkCategoryNameIsUnique(category.getName(), null);
        Category saved = categoryRepository.save(newCategory);
        return mapper.categoryToCategoryDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long catId) {
        getCategoryIfExists(catId);
        checkNoEventWithCategoryExists(catId);
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto, Long catId) {
        Category category = getCategoryIfExists(catId);
        checkCategoryNameIsUnique(categoryDto.getName(), category.getName());
        updateCategoryByDto(category, categoryDto);
        return mapper.categoryToCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> get(Integer from, Integer size) {
        Pageable pageable = pageRequestOf(from, size);
        Page<Category> categories = categoryRepository.findAll(pageable);
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

    private void checkCategoryNameIsUnique(String newName, String name) {
        if (newName.equals(name)) {
            return;
        }
        Optional<Category> catWithNameExist = categoryRepository.findFirst1ByName(newName);
        if (catWithNameExist.isPresent()) {
            throw new EWMConflictException("Имя категории уже существует.");
        }
    }

    private void checkNoEventWithCategoryExists(Long catId) {
        Optional<Event> eventWitCat = eventRepository.findByCategoryId(catId);
        if (eventWitCat.isPresent()) {
            throw new EWMConflictException("Категория не может быть удалена, так как с ней связаны существющие события.");
        }
    }

    private Category getCategoryIfExists(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new EWMElementNotFoundException(CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private void updateCategoryByDto(Category category, CategoryDto categoryDto) {
        String name = categoryDto.getName();
        if (Objects.nonNull(name)) {
            category.setName(categoryDto.getName());
        }
    }
}
