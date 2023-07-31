package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;

public interface EventRepository extends JpaRepository<Event, Long>, CustomEventRepository {

    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);
}
