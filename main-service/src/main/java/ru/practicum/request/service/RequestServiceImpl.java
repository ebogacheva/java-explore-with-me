package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.EWMConflictException;
import ru.practicum.exception.EWMElementNotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.utils.EWMCommonConstants.EVENT_NOT_FOUND_EXCEPTION_MESSAGE;
import static ru.practicum.utils.EWMCommonConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestMapper mapper;


    @Override
    public List<ParticipationRequestDto> get(Long userId) {
        getUserIfExists(userId);
        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(mapper::participationRequestToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        checkIfRequestExists(userId, eventId);
        Event event = getEventIfExists(eventId);
        checkUserIsInitiator(userId, event);
        checkEventIsPublished(event);
        checkParticipantsLimitIsReached(event);
        ParticipationRequest request = completeNewRequest(userId, event);
        return mapper.participationRequestToParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        getUserIfExists(userId);
        ParticipationRequest request = getRequestIfExists(requestId);
        request.setStatus(RequestStatus.CANCELED);
        return mapper.participationRequestToParticipationRequestDto(requestRepository.save(request));
    }

    private ParticipationRequest completeNewRequest(Long userId, Event event) {
        User user = getUserIfExists(userId);
        RequestStatus status = event.getRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
        return ParticipationRequest.builder()
                .requester(user)
                .status(status)
                .event(event)
                .build();
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EWMElementNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private ParticipationRequest getRequestIfExists(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new EWMElementNotFoundException("Такого запроса на участие не существет."));
    }

    private void checkIfRequestExists(Long userId, Long eventId) {
        if (requestRepository.findFirst1ByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new EWMConflictException("Такой запрос на участие уже существует.");
        }
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EWMElementNotFoundException(EVENT_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private static void checkUserIsInitiator(Long userId, Event event) {
        Long initiatorId = event.getInitiator().getId();
        if(userId.equals(initiatorId)) {
            throw new EWMConflictException("Пользователь не может добавить запрос на участие в своем событии.");
        }
    }

    private static void checkEventIsPublished(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new EWMConflictException("Пользователь не может добавить запрос на участие в неопубликованном событии.");
        }
    }

    private void checkParticipantsLimitIsReached(Event event) {
        Long requests = requestRepository.countByEventId(event.getId());
        Long limit = event.getParticipantLimit();
        if (requests >= limit) {
            throw new EWMConflictException("Достигнут лимит участников в событии.");
        }
    }
}
