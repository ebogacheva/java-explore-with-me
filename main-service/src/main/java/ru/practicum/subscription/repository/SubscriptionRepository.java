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

    String EVENTS_BY_CONFIRMED_PARTICIPANT =
            "select distinct e from requests r " +
                    "inner join events e on r.event.id = e.id " +
                    "where r.requester.id = ?1 and r.status = ru.practicum.enums.RequestStatus.CONFIRMED";

    String EVENTS_BY_OWNER =
            "select distinct e from events e " +
                    "where e.initiator.id = ?1 and e.state = ru.practicum.enums.EventState.PUBLISHED";

    String PUBLISHED_EVENTS_FROM_ALL_SUBSCRIBED =
            "select distinct e from subscriptions s " +
                    "inner join events e on s.user2Id = e.initiator.id " +
                    "where e.state = ru.practicum.enums.EventState.PUBLISHED " +
                    "and s.user1Id = ?1";

    String PUBLISHED_EVENTS_FROM_BY_ALL_CONFIRMED_PARTICIPANTS_SUBSCRIBED =
            "select distinct e from events e " +
                    "inner join requests r on r.event.id = e.id " +
                    "inner join subscriptions s on s.user2Id = r.requester.id " +
                    "where s.user1Id = ?1 and r.status = ru.practicum.enums.RequestStatus.CONFIRMED";

    String USER2_BY_USER1_AND_TYPE =
            " select u from users u " +
                    " join subscriptions s on u.id = s.user2Id " +
                    " where s.user1Id = ?1 and s.type = ?2";

    String USER1_BY_USER2_AND_TYPE =
            " select u from users u " +
                    " join subscriptions s on u.id = s.user1Id " +
                    " where s.user2Id = ?1 and s.type = ?2";

    @Query(USER2_BY_USER1_AND_TYPE)
    List<User> getUsersSubscribed(Long user1Id, SubscriptionType type);

    @Query(USER1_BY_USER2_AND_TYPE)
    List<User> getSubscribers(Long user2Id, SubscriptionType type);

    @Query(PUBLISHED_EVENTS_FROM_ALL_SUBSCRIBED)
    List<Event> getPublishedEventsFromAllUsersSubscribed(Long user1Id);

    @Query(PUBLISHED_EVENTS_FROM_BY_ALL_CONFIRMED_PARTICIPANTS_SUBSCRIBED)
    List<Event> getParticipantEventsFromAllUsersSubscribed(Long user1Id);

    @Query(EVENTS_BY_CONFIRMED_PARTICIPANT)
    List<Event> getEventsByParticipant(Long requesterId);

    @Query(EVENTS_BY_OWNER)
    List<Event> getEventsByOwner(Long initiatorId);

    Optional<Subscription> getByUser1IdAndUser2IdAndType(Long user1Id, Long user2Id, SubscriptionType type);
}
