package ru.practicum.subscription.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SubscriptionType;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ExploreConflictException;
import ru.practicum.exception.ExploreNotFoundException;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.subscription.repository.SubscriptionRepository;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

import static ru.practicum.enums.SubscriptionType.EVENTS;
import static ru.practicum.enums.SubscriptionType.PARTICIPATIONS;
import static ru.practicum.utils.ExploreConstantsAndStaticMethods.SUBSCRIPTION_NOT_FOUND;
import static ru.practicum.utils.ExploreConstantsAndStaticMethods.USER_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EventMapper eventMapper;


    @Override
    public void subscribe(Long userId, Long otherId, SubscriptionType type) {
        checkUsersExistenceById(userId, otherId);
        if (subRepository.getByUser1IdAndUser2IdAndType(userId, otherId, type).isEmpty()) {
            Subscription newSub = createNewSubscription(userId, otherId, type);
            subRepository.save(newSub);
        } else {
            throw new ExploreConflictException("Подписка уже существует.");
        }
    }

    @Override
    public void cancel(Long userId, Long otherId, SubscriptionType type) {
        Subscription sub = getSubscriptionIfExists(userId, otherId, type);
        subRepository.delete(sub);
    }

    @Override
    public List<UserShortDto> get(Long userId, SubscriptionType type) {
        List<User> subscriptions = subRepository.getUsersSubscribed(userId, type);
        return userMapper.toShortDtoList(subscriptions);
    }

    @Override
    public List<EventShortDto> getSubscriptions(Long userId, Long otherId, SubscriptionType type) {
        return  (type == SubscriptionType.EVENTS)
                ? getEventsByOwner(userId, otherId)
                : getEventsByParticipant(userId, otherId);
    }

    @Override
    public List<EventShortDto> getSubscriptions(Long userId, SubscriptionType type) {
        checkUsersExistenceById(userId);
        List<Event> events = (type == SubscriptionType.EVENTS)
                ? subRepository.getPublishedEventsFromAllUsersSubscribed(userId)
                : subRepository.getParticipantEventsFromAllUsersSubscribed(userId);
        return eventMapper.toEventShortDtoListFromEvents(events);
    }

    private List<EventShortDto> getEventsByOwner(Long userId, Long otherId) {
        getSubscriptionIfExists(userId, otherId, EVENTS);
        return eventMapper.toEventShortDtoListFromEvents(
                eventRepository.findByInitiatorIdAndState(otherId, EventState.PUBLISHED)
        );
    }

    private List<EventShortDto> getEventsByParticipant(Long userId, Long otherId) {
        getSubscriptionIfExists(userId, otherId, PARTICIPATIONS);
        return eventMapper.toEventShortDtoListFromEvents(
                subRepository.getEventsByParticipant(otherId)
        );
    }

    private Subscription getSubscriptionIfExists(Long userId, Long otherId, SubscriptionType type) {
        return subRepository.getByUser1IdAndUser2IdAndType(userId, otherId, type)
                .orElseThrow(() -> new ExploreNotFoundException(SUBSCRIPTION_NOT_FOUND));
    }

    private Subscription createNewSubscription(Long userId, Long otherId, SubscriptionType type) {
        return Subscription.builder()
                .user1Id(userId)
                .user2Id(otherId)
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
