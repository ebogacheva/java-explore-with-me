package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.EventState;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class EventFilterParamsDto {
    @Builder.Default
    private List<Long> ids = List.of();
    @Builder.Default
    private List<EventState> states = List.of();
    @Builder.Default
    private List<Long> categories = List.of();
    private String rangeStart;
    private String rangeEnd;
    @Builder.Default
    private Integer from = 0;
    @Builder.Default
    private Integer size = 10;
    private String text;
    private Boolean paid;
    private Boolean onlyAvailable;
    private EventSort sort;
}
