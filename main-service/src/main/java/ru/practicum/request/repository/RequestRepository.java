package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);
    List<ParticipationRequest> findByEventId(Long eventId);
    List<ParticipationRequest> findByRequesterId(Long userId);
    Optional<ParticipationRequest> findFirst1ByEventIdAndRequesterId(Long eventId, Long userId);
    List<ParticipationRequest> findAllByIdIn(List<Long> ids);
}
