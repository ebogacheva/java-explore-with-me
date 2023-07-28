package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.service.EventServiceImpl;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final EventServiceImpl eventService;

    @GetMapping
    List<EventFullDto> get(@ModelAttribute EventFilterParams params) {
        return eventService.get(params);
    }

    @PatchMapping(value = "/{eventId}")
    EventFullDto update(@PathVariable Long eventId,
                        @RequestBody UpdateEventAdminRequest request) {
        return eventService.update(request, eventId);
    }


}
