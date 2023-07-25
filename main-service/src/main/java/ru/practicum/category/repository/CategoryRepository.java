package ru.practicum.category.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<CategoryDto> findCategories(Pageable pageable);
}
