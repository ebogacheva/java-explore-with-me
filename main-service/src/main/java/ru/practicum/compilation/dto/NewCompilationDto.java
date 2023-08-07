package ru.practicum.compilation.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class NewCompilationDto {

    List<Long> events = List.of();
    Boolean pinned = Boolean.FALSE;
    @NotNull
    @NotBlank
    @Length(min = 1, max = 50)
    private String title;
}
