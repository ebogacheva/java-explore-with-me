package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFilterParamsDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.service.EventServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final EventServiceImpl eventService;

    @GetMapping
    List<EventFullDto> getByAdmin(@Valid @ModelAttribute EventFilterParamsDto params) {
        return eventService.getByAdmin(params);
    }

    @PatchMapping(value = "/{eventId}")
    EventFullDto update(@PathVariable Long eventId,
                        @RequestBody UpdateEventAdminRequest request) {
        return eventService.update(request, eventId);
    }


}
