package ru.practicum.compilation.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UpdateCompilationRequest {

    @Length(min = 1, max = 50)
    String title;
    Boolean pinned;
    List<Long> events;
}
