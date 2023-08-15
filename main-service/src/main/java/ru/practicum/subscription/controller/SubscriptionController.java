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

    @PostMapping(value = "/{userId}/subscriptions/{ownerId}")
    public void subscribe(@PathVariable Long userId,
                          @PathVariable Long ownerId,
                          @RequestParam @NotNull SubscriptionType type) {
        subService.subscribe(userId, ownerId, type);
    }

    @DeleteMapping(value = "/{userId}/subscriptions/{ownerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long userId,
                       @PathVariable Long ownerId,
                       @RequestParam @NotNull SubscriptionType type) {
        subService.cancel(userId, ownerId, type);
    }

    @GetMapping(value = "/{userId}/subscriptions")
    public List<UserShortDto> get(@PathVariable Long userId,
                                  @RequestParam @NotNull SubscriptionType type) {
        return subService.get(userId, type);
    }

    @GetMapping(value = "/{userId}/subscriptions/events/{ownerId}")
    public List<EventShortDto> getEvents(@PathVariable Long userId,
                                         @PathVariable Long ownerId) {
        return subService.getSubscriptions(userId, ownerId, SubscriptionType.EVENTS);
    }

    @GetMapping(value = "/{userId}/subscriptions/participation/{ownerId}")
    public List<EventShortDto> getParticipation(@PathVariable Long userId,
                                        @PathVariable Long ownerId) {
        return subService.getSubscriptions(userId, ownerId, SubscriptionType.PARTICIPATIONS);
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