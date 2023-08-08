package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.enums.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ExploreConflictException;
import ru.practicum.exception.EWMElementNotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.enums.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

import static ru.practicum.utils.ExploreConstantsAndStaticMethods.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestMapper requestMapper;


    @Override
    public List<ParticipationRequestDto> get(Long userId) {
        getUserIfExists(userId);
        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        return requestMapper.toRequestDtoList(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        checkIfRequestExists(userId, eventId);
        Event event = getEventIfExists(eventId);
        checkUserIsInitiator(userId, event);
        checkEventIsPublished(event);
        if (event.getParticipantLimit() > 0) {
            checkParticipantsLimitIsReached(event);
        }
        ParticipationRequest request = completeNewRequest(userId, event);
        return requestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        getUserIfExists(userId);
        ParticipationRequest request = getRequestIfExists(requestId);
        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toRequestDto(requestRepository.save(request));
    }

    private ParticipationRequest completeNewRequest(Long userId, Event event) {
        User user = getUserIfExists(userId);
        boolean needConfirmation = event.getRequestModeration();
        boolean hasParticipantsLimit = event.getParticipantLimit() != 0;
        RequestStatus status = needConfirmation && hasParticipantsLimit ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
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
                .orElseThrow(() -> new EWMElementNotFoundException(PARTICIPATION_REQUEST_NOT_FOUND));
    }

    private void checkIfRequestExists(Long userId, Long eventId) {
        if (requestRepository.findFirst1ByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ExploreConflictException(REQUEST_ALREADY_EXIST);
        }
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EWMElementNotFoundException(EVENT_NOT_FOUND_EXCEPTION));
    }

    private static void checkUserIsInitiator(Long userId, Event event) {
        Long initiatorId = event.getInitiator().getId();
        if(userId.equals(initiatorId)) {
            throw new ExploreConflictException(OWNER_NOT_ALLOWED_TO_ADD_REQUEST);
        }
    }

    private static void checkEventIsPublished(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ExploreConflictException(INVALID_EVENT_STATUS);
        }
    }

    private void checkParticipantsLimitIsReached(Event event) {
        Long participants = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        Long limit = event.getParticipantLimit();
        if (participants >= limit) {
            throw new ExploreConflictException(EVENT_PARTICIPANTS_LIMIT_IS_REACHED);
        }
    }
}
