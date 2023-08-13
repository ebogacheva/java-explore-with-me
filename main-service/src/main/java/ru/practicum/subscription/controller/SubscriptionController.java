package ru.practicum.subscription.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.enums.SubscriptionType;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.subscription.service.SubscriptionServiceImpl;
import ru.practicum.user.dto.UserShortDto;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionServiceImpl subService;

    @PostMapping(value = "/{userId}/subscriptions/{otherId}")
    public void subscribe(@PathVariable Long userId,
                          @PathVariable Long otherId,
                          @RequestParam @NotNull SubscriptionType type) {
        subService.subscribe(userId, otherId, type);
    }

    @DeleteMapping(value = "/{userId}/subscriptions/{otherId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long userId,
                       @PathVariable Long otherId,
                       @RequestParam @NotNull SubscriptionType type) {
        subService.cancel(userId, otherId, type);
    }

    @GetMapping(value = "/{userId}/subscriptions")
    public List<UserShortDto> get(@PathVariable Long userId,
                                  @RequestParam @NotNull SubscriptionType type) {
        return subService.get(userId, type);
    }

    @GetMapping(value = "/{userId}/subscriptions/events/{otherId}")
    public List<EventShortDto> getEvents(@PathVariable Long userId,
                                         @PathVariable Long otherId) {
        return subService.getSubscriptions(userId, otherId, SubscriptionType.EVENTS);
    }

    @GetMapping(value = "/{userId}/subscriptions/participation/{otherId}")
    public List<EventShortDto> getParticipation(@PathVariable Long userId,
                                        @PathVariable Long otherId) {
        return subService.getSubscriptions(userId, otherId, SubscriptionType.PARTICIPATIONS);
    }

    @GetMapping(value = "/{userId}/subscriptions/events")
    public List<EventShortDto> getEvents(@PathVariable Long userId) {
        return subService.getSubscriptions(userId, SubscriptionType.EVENTS);
    }

    @GetMapping(value = "/{userId}/subscriptions/participation")
    public List<EventShortDto> getParticipation(@PathVariable Long userId) {
        return subService.getSubscriptions(userId, SubscriptionType.PARTICIPATIONS);
    }
}