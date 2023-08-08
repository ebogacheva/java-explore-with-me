package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ExploreConflictException;
import ru.practicum.exception.ExploreNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.practicum.utils.ExploreConstantsAndStaticMethods.*;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper catMapper;

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto category) {
        Category newCategory = catMapper.toCategory(category);
        checkNewCatNameIsUnique(category.getName());
        Category saved = categoryRepository.save(newCategory);
        return catMapper.toCategoryDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long catId) {
        Category category = getCategoryIfExists(catId);
        checkNoEventWithCatExists(catId);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto, Long catId) {
        Category category = getCategoryIfExists(catId);
        checkNewCatNameIsUnique(categoryDto.getName(), category.getName());
        updateCategoryByDto(category, categoryDto);
        return catMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> get(Integer from, Integer size) {
        Page<Category> catPage = categoryRepository.findAll(pageRequestOf(from, size));
        return catPage.map(catMapper::toCategoryDto).getContent();
    }

    @Override
    public CategoryDto get(Long catId) {
        Category category = getCategoryIfExists(catId);
        return catMapper.toCategoryDto(category);
    }

    private void checkNewCatNameIsUnique(String newName, String name) {
        if (!newName.equals(name)) {
            categoryRepository.findFirst1ByName(newName).ifPresent(cat -> {
                throw new ExploreConflictException(CATEGORY_NAME_ALREADY_EXISTS_EXCEPTION);
            });
        }
    }

    private void checkNewCatNameIsUnique(String newName) {
        checkNewCatNameIsUnique(newName, null);
    }

    private void checkNoEventWithCatExists(Long catId) {
        Optional<Event> eventWithCat = eventRepository.findByCategoryId(catId);
        if (eventWithCat.isPresent()) {
            throw new ExploreConflictException(CATEGORY_IS_CONNECTED_WITH_EVENTS);
        }
    }

    private Category getCategoryIfExists(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new ExploreNotFoundException(CATEGORY_NOT_FOUND_EXCEPTION));
    }

    private void updateCategoryByDto(Category category, CategoryDto categoryDto) {
        String newName = categoryDto.getName();
        String existingName = category.getName();
        category.setName(Objects.nonNull(newName) ? newName : existingName);
    }
}
