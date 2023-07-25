package ru.practicum.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class CategoryDto {

    private Long id;
    @Length(min = 1, max = 50)
    private String name;
}
