package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.EWMElementNotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.utils.EWMCommonConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE;
import static ru.practicum.utils.EWMCommonMethods.pageRequestOf;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EventMapper mapper;

    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        getUserIfExists(userId);
        Pageable pageable = pageRequestOf(from, size);
        Page<EventShortDto> pageOfEvents = eventRepository.findByInitiatorId(userId, pageable);
        List<EventShortDto> listOfEvents = mapper.pageToList(pageOfEvents);
        return listOfEvents.stream().map(this::completeEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto create(NewEventDto newEventDto, Long userId) {
        return null;
    }

    @Override
    public EventFullDto getEvent(Long userId, Long eventId) {
        return null;
    }

    @Override
    public EventFullDto update(UpdateEventUserRequest updateRequest, Long userId, Long eventId) {
        return null;
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        return null;
    }

    @Override
    public EventFullDto update(EventRequestStatusUpdateRequest updateRequest, Long userId, Long eventId) {
        return null;
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EWMElementNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private EventShortDto completeEventShortDto(EventShortDto eventShortDto) {
        Long eventId = eventShortDto.getId();
        Long confirmedRequests = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        Long views = -1L; //TODO: get veiws!!
        eventShortDto.setConfirmedRequests(confirmedRequests);
        eventShortDto.setViews(views);
        return eventShortDto;
    }
}
