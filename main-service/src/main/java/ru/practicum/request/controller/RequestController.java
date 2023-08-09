package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @GetMapping(value = "/{userId}/requests")
    public List<ParticipationRequestDto> get(@PathVariable Long userId) {
        return requestService.get(userId);
    }

    @PostMapping(value = "/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable Long userId,
                                   @RequestParam Long eventId) {
        return requestService.create(userId, eventId);
    }

    @PatchMapping(value = "/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable Long userId,
                                   @PathVariable Long requestId) {
        return requestService.cancel(userId, requestId);
    }

}
