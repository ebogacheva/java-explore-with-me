package ru.practicum.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.enums.SubscriptionType;
import ru.practicum.event.model.Event;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    String EVENTS_BY_CONFIRMED_PARTICIPANT_SUBSCRIBED =
            "select distinct e from requests r " +
                    "inner join events e on r.event.id = e.id " +
                    "where r.requester.id = ?1 and r.status = ru.practicum.enums.RequestStatus.CONFIRMED";

    String EVENTS_BY_OWNER_SUBSCRIBED =
            "select distinct e from events e " +
                    "where e.initiator.id = ?1 and e.state = ru.practicum.enums.EventState.PUBLISHED";

    String OWNERS_EVENTS_FROM_ALL_SUBSCRIBED =
            "select distinct e from subscriptions s " +
                    "inner join events e on s.owner = e.initiator.id " +
                    "where e.state = ru.practicum.enums.EventState.PUBLISHED " +
                    "and s.subscriber = ?1";

    String EVENTS_FROM_BY_ALL_CONFIRMED_PARTICIPANTS_SUBSCRIBED =
            "select distinct e from events e " +
                    "inner join requests r on r.event.id = e.id " +
                    "inner join subscriptions s on s.owner = r.requester.id " +
                    "where s.subscriber = ?1 and r.status = ru.practicum.enums.RequestStatus.CONFIRMED";

    String OWNERS_BY_SUBSCRIBER_AND_TYPE =
            " select u from users u " +
                    " join subscriptions s on u.id = s.owner " +
                    " where s.subscriber = ?1 and s.type = ?2";

    String SUBSCRIBERS_BY_OWNER_AND_TYPE =
            " select u from users u " +
                    " join subscriptions s on u.id = s.subscriber " +
                    " where s.owner = ?1 and s.type = ?2";

    @Query(OWNERS_BY_SUBSCRIBER_AND_TYPE)
    List<User> getUsersSubscribed(Long user1Id, SubscriptionType type);

    @Query(SUBSCRIBERS_BY_OWNER_AND_TYPE)
    List<User> getSubscribers(Long owner, SubscriptionType type);

    @Query(OWNERS_EVENTS_FROM_ALL_SUBSCRIBED)
    List<Event> getOwnersEventsFromAllSubscribed(Long subscriber);

    @Query(EVENTS_FROM_BY_ALL_CONFIRMED_PARTICIPANTS_SUBSCRIBED)
    List<Event> getParticipantEventsFromAllSubscribed(Long subscriber);

    @Query(EVENTS_BY_CONFIRMED_PARTICIPANT_SUBSCRIBED)
    List<Event> getEventsByParticipant(Long requesterId);

    @Query(EVENTS_BY_OWNER_SUBSCRIBED)
    List<Event> getEventsByOwner(Long initiatorId);

    Optional<Subscription> getBySubscriberAndOwnerAndType(Long subscriber, Long owner, SubscriptionType type);
}
