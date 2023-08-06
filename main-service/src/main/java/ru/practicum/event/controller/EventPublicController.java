package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFilterParamsDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventServiceImpl eventService;

    @GetMapping
    List<EventShortDto> get(@Valid @ModelAttribute EventFilterParamsDto params,
                            HttpServletRequest request) {
        return eventService.getByPublic(params, request);
    }

    @GetMapping(value = "/{id}")
    EventFullDto get(@PathVariable Long id) {
        return eventService.get(id);
    }

}
