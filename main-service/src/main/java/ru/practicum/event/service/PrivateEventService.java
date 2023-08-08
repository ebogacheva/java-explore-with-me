package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateEventService {

    List<EventShortDto> getEventsByPrivate(Long userId, Integer from, Integer size);

    EventFullDto createByPrivate(NewEventDto eventDto, Long userId);

    EventFullDto getEventByPrivate(Long userId, Long eventId);

    EventFullDto updateByPrivate(UpdateEventUserRequest updateRequest, Long userId, Long eventId);

    List<ParticipationRequestDto> getRequestsByPrivate(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateByPrivate(EventRequestStatusUpdateRequest updateRequest, Long userId, Long eventId);
}

