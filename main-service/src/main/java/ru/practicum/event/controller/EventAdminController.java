package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFilterParamsDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.service.AdminEventService;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final AdminEventService eventService;

    @GetMapping
    List<EventFullDto> getByAdmin(@Valid @ModelAttribute EventFilterParamsDto params) {
        return eventService.getEventsByAdmin(params);
    }

    @PatchMapping(value = "/{eventId}")
    EventFullDto update(@PathVariable Long eventId,
                        @Valid @RequestBody UpdateEventAdminRequest request) {
        return eventService.updateEventByAdmin(request, eventId);
    }
}
