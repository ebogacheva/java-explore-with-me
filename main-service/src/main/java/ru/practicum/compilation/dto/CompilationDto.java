package ru.practicum.compilation.dto;

import lombok.*;
import ru.practicum.event.dto.EventShortDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CompilationDto {

    private Long id;
    private String title;
    private Boolean pinned;
    private List<EventShortDto> events = new ArrayList<>();
}
