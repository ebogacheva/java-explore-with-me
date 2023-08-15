package ru.practicum.subscription.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.enums.SubscriptionType;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ExploreConflictException;
import ru.practicum.exception.ExploreNotFoundException;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.subscription.repository.SubscriptionRepository;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static ru.practicum.enums.SubscriptionType.EVENTS;
import static ru.practicum.enums.SubscriptionType.PARTICIPATIONS;
import static ru.practicum.utils.ExploreConstantsAndStaticMethods.*;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EventMapper eventMapper;


    @Override
    @Transactional
    public void subscribe(Long userId, Long ownerId, SubscriptionType type) {
        checkUsersExistenceById(userId, ownerId);
        if (getSubscription(userId, ownerId, type).isEmpty()) {
            Subscription newSub = createNewSubscription(userId, ownerId, type);
            subRepository.save(newSub);
        } else {
            throw new ExploreConflictException(SUBSCRIPTION_ALREADY_EXISTS);
        }
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long ownerId, SubscriptionType type) {
        Subscription sub = getSubscriptionIfExists(userId, ownerId, type);
        subRepository.delete(sub);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserShortDto> get(Long userId, SubscriptionType type) {
        List<User> subscriptions = subRepository.getUsersSubscribed(userId, type);
        return userMapper.toShortDtoList(subscriptions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getSubscriptions(Long userId, Long ownerId, SubscriptionType type) {
        return  (type == SubscriptionType.EVENTS)
                ? getEventsByOwner(userId, ownerId)
                : getEventsByParticipant(userId, ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getSubscriptions(Long userId, SubscriptionType type) {
        checkUsersExistenceById(userId);
        List<Event> events = (type == SubscriptionType.EVENTS)
                ? subRepository.getOwnersEventsFromAllSubscribed(userId)
                : subRepository.getParticipantEventsFromAllSubscribed(userId);
        return eventMapper.toEventShortDtoListFromEvents(events);
    }

    private List<EventShortDto> getEventsByOwner(Long userId, Long ownerId) {
        getSubscriptionIfExists(userId, ownerId, EVENTS);
        return eventMapper.toEventShortDtoListFromEvents(
                subRepository.getEventsByOwner(ownerId)
        );
    }

    private List<EventShortDto> getEventsByParticipant(Long userId, Long ownerId) {
        getSubscriptionIfExists(userId, ownerId, PARTICIPATIONS);
        return eventMapper.toEventShortDtoListFromEvents(
                subRepository.getEventsByParticipant(ownerId)
        );
    }

    private Subscription getSubscriptionIfExists(Long userId, Long ownerId, SubscriptionType type) {
        return subRepository.getBySubscriberAndOwnerAndType(userId, ownerId, type)
                .orElseThrow(() -> new ExploreNotFoundException(SUBSCRIPTION_NOT_FOUND));
    }

    private Optional<Subscription> getSubscription(Long userId, Long ownerId, SubscriptionType type) {
        return subRepository.getBySubscriberAndOwnerAndType(userId, ownerId, type);
    }

    private Subscription createNewSubscription(Long userId, Long ownerId, SubscriptionType type) {
        return Subscription.builder()
                .subscriber(userId)
                .owner(ownerId)
                .type(type)
                .build();
    }

    public void checkUsersExistenceById(Long...userIds) {
        for (Long id : userIds) {
            getUserIfExists(id);
        }
    }

    private void getUserIfExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ExploreNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
    }
}
