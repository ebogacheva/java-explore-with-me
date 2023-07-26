package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.EventStateAction;
import ru.practicum.event.model.Location;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.EWMElementNotFoundException;
import ru.practicum.exception.EWMUpdateForbiddenException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.utils.EWMCommonConstants.*;
import static ru.practicum.utils.EWMCommonMethods.pageRequestOf;
import static ru.practicum.utils.EWMDateTimeFormatter.stringToLocalDateTime;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        getUserIfExists(userId);
        Pageable pageable = pageRequestOf(from, size);
        Page<EventShortDto> pageOfEvents = eventRepository.findByInitiatorId(userId, pageable);
        List<EventShortDto> listOfEvents = eventMapper.pageToList(pageOfEvents);
        return listOfEvents.stream().map(this::completeEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto create(NewEventDto newEventDto, Long userId) {
        Event newEvent = completeNewEvent(newEventDto, userId);
        Event savedEvent = eventRepository.save(newEvent);
        return completeEventFullDto(savedEvent);
    }

    @Override
    public EventFullDto getEvent(Long userId, Long eventId) {
        getUserIfExists(userId); // TODO: do we need this checking?
        Event event = getEventIfExists(eventId); // TODO: do we need to check that user is an initiator of the event?
        return completeEventFullDto(event);
    }

    @Override
    public EventFullDto update(UpdateEventUserRequest updateRequest, Long userId, Long eventId) {
        getUserIfExists(userId); // TODO: do we need this checking?
        Event event = getEventIfExists(eventId); // TODO: do we need to check that user is an initiator of the event?
        return updateEvent(updateRequest, event);
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        getUserIfExists(userId);
        return requestRepository.findByEventId(eventId);
    }

    @Override
    public EventRequestStatusUpdateResult update(EventRequestStatusUpdateRequest updateRequest, Long userId, Long eventId) {
        getUserIfExists(userId); // TODO: do we need this checking?
        Event event = getEventIfExists(eventId); // TODO: do we need to check that user is an initiator of the event?
        List<Long> ids = updateRequest.getRequestIds();
        RequestStatus status = RequestStatus.valueOf(updateRequest.getRequestStatus());
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        if (!event.getRequestModeration()) {
            return result; // TODO: если подтверждение заявок не требуется, что должно быть в реультате?
        }
        if (event.getParticipantLimit() == 0) {
            return result;
        }
        if (status == RequestStatus.CONFIRMED) {
            confirmRequests(ids, result);
        } else if (status == RequestStatus.REJECTED) {
            rejectRequests(ids, result);
        }
        return result;
    }
    private void confirmRequests(List<Long> ids, EventRequestStatusUpdateResult result) {

    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EWMElementNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EWMElementNotFoundException(EVENT_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private Category getCategoryIfExists(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new EWMElementNotFoundException(CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private Location getLocation(LocationDto locationDto) {
        Location location = locationMapper.locationDtoToLocation(locationDto);
        return locationRepository.getByLatAndLot(location.getLat(), location.getLot())
                .orElse(locationRepository.save(location));
    }

    private EventShortDto completeEventShortDto(EventShortDto eventShortDto) {
        Long eventId = eventShortDto.getId();
        Long confirmedRequests = getConfirmedRequests(eventId);
        Long views = -1L; //TODO: get veiws!!
        eventShortDto.setConfirmedRequests(confirmedRequests);
        eventShortDto.setViews(views);
        return eventShortDto;
    }

    private Event completeNewEvent(NewEventDto newEventDto, Long userId) {
        Event event = eventMapper.newEventDtoToEvent(newEventDto);
        User user = getUserIfExists(userId);
        Category category = getCategoryIfExists(newEventDto.getCatId()); // TODO make joins with corresponding tables
        Location location = getLocation(newEventDto.getLocationDto());
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setState(EventState.PENDING);
        return event;
    }

    private EventFullDto completeEventFullDto(Event event) {
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event);
        Long confirmedRequests = getConfirmedRequests(event.getId());
        Long views = -1L; //TODO: get veiws!!
        eventFullDto.setConfirmedRequests(confirmedRequests);
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    private Long getConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private EventFullDto updateEvent(UpdateEventUserRequest request, Event event) {
        checkEventIsCanceledOrPending(event);
        updateEventAnnotation(request.getAnnotation(), event);
        updateEventCategory(request.getCategory(), event);
        updateEventDescription(request.getDescription(), event);
        updateEventDate(request.getEventDate(), event);
        updateEventLocation(request.getLocationDto(), event);
        updateEventPaidStatus(request.getPaid(), event);
        updateEventParticipationLimit(request.getParticipantLimit(), event);
        updateEventRequestModeration(request.getRequestModeration(), event);
        updateEventStateAction(request.getEventStateAction(), event);
        return eventMapper.eventToEventFullDto(event);
    }

    private void updateEventStateAction(String action, Event event) {
        if (Objects.nonNull(action)) {
            EventStateAction updatedAction = EventStateAction.fromString(action);
            if (updatedAction == EventStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (updatedAction == EventStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }
    }

    private void updateEventRequestModeration(Boolean requestModeration, Event event) {
        if (Objects.nonNull(requestModeration)) {
            event.setRequestModeration(requestModeration);
        }
    }

    private void updateEventParticipationLimit(Integer limit, Event event) {
        if (Objects.nonNull(limit)) {
            event.setParticipantLimit(limit);
        }
    }

    private void updateEventPaidStatus(Boolean paid, Event event) {
        if (Objects.nonNull(paid)) {
            event.setPaid(paid);
        }
    }

    private void updateEventLocation(LocationDto locationDto, Event event) {
        if (Objects.nonNull(locationDto)) {
            Location updatedLocation = getLocation(locationDto);
            event.setLocation(updatedLocation);
        }
    }

    private void updateEventDate(String eventDate, Event event) {
        if (Objects.nonNull(eventDate)) {
            LocalDateTime updatedEventDate = stringToLocalDateTime(eventDate);
            LocalDateTime minEventDateTime = LocalDateTime.now().plus(2, ChronoUnit.HOURS);
            if(Objects.nonNull(updatedEventDate) && updatedEventDate.isAfter(minEventDateTime)) {
                event.setEventDate(updatedEventDate);
            }
        }
    }

    private void updateEventDescription(String description, Event event) {
        if (Objects.nonNull(description)) {
            event.setDescription(description);
        }
    }

    private void updateEventCategory(Long catId, Event event) {
        if (Objects.nonNull(catId)) {
            Category updated = getCategoryIfExists(catId);
            event.setCategory(updated);
        }
    }

    private void updateEventAnnotation(String annotation, Event event) {
        if(Objects.nonNull(annotation)) {
            event.setAnnotation(annotation);
        }
    }

    private void checkEventIsCanceledOrPending(Event event) {
        boolean canceled = event.getState() == EventState.CANCELED;
        boolean pending = event.getState() == EventState.PENDING;
        if (!canceled && !pending) {
            throw new EWMUpdateForbiddenException(EVENT_UPDATING_IS_FORBIDDEN_EXCEPTION_MESSAGE);
        }
    }
}