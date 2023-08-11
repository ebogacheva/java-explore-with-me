package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.PrivateEventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class EventPrivateController {

    private final PrivateEventService eventService;

    @PostMapping(value = "/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                        @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.createByPrivate(newEventDto, userId);
    }

    @GetMapping(value = "/{userId}/events")
    public List<EventShortDto> getEvents(@PathVariable Long userId,
                                  @RequestParam(required = false, defaultValue = "0") Integer from,
                                  @RequestParam(required = false, defaultValue = "10") Integer size) {
        return eventService.getEventsByPrivate(userId, from, size);
    }

    @GetMapping(value = "/{userId}/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId,
                     @PathVariable Long eventId) {
        return eventService.getEventByPrivate(userId, eventId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}")
    public EventFullDto update(@PathVariable Long userId,
                        @PathVariable Long eventId,
                        @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        return eventService.updateByPrivate(updateRequest, userId, eventId);
    }

    @GetMapping(value = "/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable Long userId,
                                      @PathVariable Long eventId) {
        return eventService.getRequestsByPrivate(userId, eventId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult update(@PathVariable Long userId,
                                          @PathVariable Long eventId,
                                          @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return eventService.updateByPrivate(updateRequest, userId, eventId);
    }
}
