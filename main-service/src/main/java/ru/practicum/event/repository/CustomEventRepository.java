package ru.practicum.event.repository;

import ru.practicum.event.dto.EventFilterParams;
import ru.practicum.event.model.Event;

import java.util.List;

public interface CustomEventRepository {

    List<Event> adminEventsSearch(EventFilterParams params);

    List<Event> publicEventsSearch(EventFilterParams params);
}
