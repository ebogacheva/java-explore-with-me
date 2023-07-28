package ru.practicum.event.controller;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EventFilterParams {

    private List<Long> ids;
    private List<EventState> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Integer from;
    private Integer size;
}
