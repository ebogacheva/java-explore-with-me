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
                    "join events e on r.eventId = e.id " +
                    "where r.requesterId = ?1 AND r.status = ru.practicum.enums.RequestStatus.CONFIRMED";

    String USER2_BY_USER1_AND_TYPE =
            " select u from users u " +
                    " inner join subscriptions s on u.id = s.user2_id " +
                    " where s.user1_id = ?1 and type =?2";

    @Query(USER2_BY_USER1_AND_TYPE)
    List<User> getUsersSubscribed(Long user1Id, SubscriptionType type);

    @Query(EVENTS_BY_USER1_AND_TYPE)
    List<Event> getEventsFromUsersSubscribed(Long user1Id, SubscriptionType type);

    @Query(EVENTS_BY_CONFIRMED_PARTICIPANT)
    List<Event> getEventsByParticipant(Long requesterId);

    Optional<Subscription> getByUser1IdUser2IdAndType(Long user1Id, Long user2Id, SubscriptionType type);
}
