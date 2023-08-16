package ru.practicum.subscription.service;

import ru.practicum.enums.SubscriptionType;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

public interface SubscriptionService {

    void subscribe(Long userId, Long ownerId, SubscriptionType type);

    void cancel(Long userId, Long ownerId, SubscriptionType type);

    List<UserShortDto> get(Long userId, SubscriptionType type);

    List<EventShortDto> getSubscriptions(Long userId, Long ownerId, SubscriptionType type);

    List<EventShortDto> getSubscriptions(Long userId, SubscriptionType type);
}
