package ru.practicum.event.repository;

import ru.practicum.event.controller.EventFilterParams;
import ru.practicum.event.model.Event;

import java.util.List;

public interface CustomEventRepository {

    List<Event> findEventsByAdmin(EventFilterParams params);
}
