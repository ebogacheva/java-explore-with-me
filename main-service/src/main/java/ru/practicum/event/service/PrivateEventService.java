package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateEventService {

    List<EventShortDto> getEvents(Long userId, Integer from, Integer size);

    EventFullDto create(NewEventDto eventDto, Long userId);

    EventFullDto getEvent(Long userId, Long eventId);

    EventFullDto update(UpdateEventUserRequest updateRequest, Long userId, Long eventId);

    List<ParticipationRequestDto> getRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult update(EventRequestStatusUpdateRequest updateRequest, Long userId, Long eventId);
}

