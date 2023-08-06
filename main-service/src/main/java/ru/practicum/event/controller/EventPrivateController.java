package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventServiceImpl;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class EventPrivateController {

    private final EventServiceImpl eventService;

    @GetMapping(value = "/{userId}/events")
    List<EventShortDto> getEvents(@PathVariable Long userId,
                            @RequestParam(required = false, defaultValue = "0") Integer from,
                            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return eventService.getEvents(userId, from, size);
    }

    @PostMapping(value = "/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto create(@PathVariable Long userId,
                        @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.create(newEventDto, userId);
    }

    @GetMapping(value = "/{userId}/events/{eventId}")
    EventFullDto getEvent(@PathVariable Long userId,
                     @PathVariable Long eventId) {
        return eventService.getEvent(userId, eventId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}")
    EventFullDto update(@PathVariable Long userId,
                        @PathVariable Long eventId,
                        @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        return eventService.update(updateRequest, userId, eventId);
    }

    @GetMapping(value = "/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> getRequests(@PathVariable Long userId,
                                      @PathVariable Long eventId) {
        return eventService.getRequests(userId, eventId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult update(@PathVariable Long userId,
                                          @PathVariable Long eventId,
                                          @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return eventService.update(updateRequest, userId, eventId);
    }




}
