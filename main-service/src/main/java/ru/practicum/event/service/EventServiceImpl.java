package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.EventFilterParamsDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.*;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.EWMConflictException;
import ru.practicum.exception.EWMElementNotFoundException;
import ru.practicum.exception.EWMRequestConfirmForbiddenException;
import ru.practicum.exception.EWMUpdateForbiddenException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.utils.EWMDateTimeFormatter;
import ru.practicum.utils.EWMTimeDecoderUrl;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Comparator;
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
    private final ParticipationRequestMapper participationRequestMapper;
    private final StatService statService;

    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        getUserIfExists(userId);
        Pageable pageable = pageRequestOf(from, size);
        Page<Event> pageOfEvents = eventRepository.findByInitiatorId(userId, pageable);
        List<Event> listOfEvents = eventMapper.pageToList(pageOfEvents);
        return listOfEvents.stream()
                .map(eventMapper::eventToEventShortDto)
                .map(this::completeEventShortDto)
                .collect(Collectors.toList());
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
    public EventFullDto update(UpdateEventUserRequest request, Long userId, Long eventId) {
        getUserIfExists(userId); // TODO: do we need this checking?
        Event event = getEventIfExists(eventId); // TODO: do we need to check that user is an initiator of the event?
        checkEventStateInList(event, List.of(EventState.PENDING, EventState.CANCELED, EventState.REJECTED));
        updateEvent(request, event);
        eventRepository.save(event);
        return eventMapper.eventToEventFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        getUserIfExists(userId);
        return requestRepository.findByEventId(eventId)
                .stream()
                .map(participationRequestMapper::participationRequestToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult update(EventRequestStatusUpdateRequest updateRequest, Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        List<Long> ids = updateRequest.getRequestIds();
        RequestStatus status = RequestStatus.valueOf(updateRequest.getRequestStatus());
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        if (!event.getRequestModeration()) {
            return result;
        }
        if (event.getParticipantLimit() == 0) {
            return result;
        }
        List<ParticipationRequest> requestsToUpdate =
                requestRepository.findByEventId(eventId)
                        .stream()
                        .filter(request -> ids.contains(request.getId()))
                        .collect(Collectors.toList());
        if (status == RequestStatus.CONFIRMED) {
            confirmRequests(requestsToUpdate, result, event);
        } else if (status == RequestStatus.REJECTED) {
            rejectRequests(requestsToUpdate, result, event);
        }
        return result;
    }

    @Override
    public List<EventFullDto> getByAdmin(EventFilterParamsDto paramsDto) {
        EventFilterParams params = convertInputParams(paramsDto);

        List<EventFullDto> events = eventRepository.adminEventsSearch(params)
                .stream()
                .map(eventMapper::eventToEventFullDto)
                .map(this::completeWithRequests)
                .map(this::completeWithViews)
                .map(eventDto -> (EventFullDto)eventDto)
                .sorted(getComparator(paramsDto.getSort()))
                .collect(Collectors.toList());
        return events;
    }

    private EventDto completeWithRequests(EventDto eventDto) {
        Long eventId = eventDto.getId();
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        eventDto.setConfirmedRequests(confirmedRequests);
        return eventDto;
    }

    private EventDto completeWithViews(EventDto eventDto) {
        Long eventId = eventDto.getId();
        Long views = statService.getViews(eventId);
        eventDto.setViews(views);
        return eventDto;
    }

    @Override
    public EventFullDto update(UpdateEventAdminRequest request, Long eventId) {
        Event event = getEventIfExists(eventId);
        checkEventDateIsAfterNow(event.getEventDate(), 1);
        if (request.getEventDate() != null) {
            LocalDateTime targetDateTime = EWMDateTimeFormatter.stringToLocalDateTime(request.getEventDate());
            if (targetDateTime != null) {
                checkEventDateIsAfterNow(targetDateTime, 2);
            }
        }
        EventStateAction action = request.getStateAction();
        if (Objects.nonNull(action)) {
            switch (action) {
                case PUBLISH_EVENT:
                    publishEvent(request, event);
                    break;
                case REJECT_EVENT:
                    rejectEvent(event);
                    break;
            }
        }
        eventRepository.save(event);
        return eventMapper.eventToEventFullDto(event);
    }



    @Override
    public List<EventShortDto> getByPublic(EventFilterParamsDto paramsDto, HttpServletRequest request) {
        EventFilterParams params = convertInputParams(paramsDto);
        statService.addHit(request);
        return eventRepository.publicEventsSearch(params)
                .stream()
                .map(eventMapper::eventToEventShortDto)
                .peek(this::completeWithRequests)
                .peek(this::completeWithViews)
                .sorted(getComparator(paramsDto.getSort()))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto get(Long eventId) {
        Event event = getEventIfExists(eventId);
        checkEventIsPublished(event);
        return completeEventFullDto(event);
    }

    private Comparator<EventDto> getComparator(EventSort sortType) {
        if (sortType == null) {
            return EventDto.EVENT_DATE_COMPARATOR;
        }
        if (sortType == EventSort.VIEWS) {
            return EventDto.VIEWS_COMPARATOR;
        }
        return EventDto.EVENT_DATE_COMPARATOR;
    }

    private void checkEventIsPublished(Event event) {
        boolean published = event.getState() == EventState.PUBLISHED;
        if(!published) {
            throw new EWMElementNotFoundException("Событие не найдено.");
        }
    }

    private EventFilterParams convertInputParams(EventFilterParamsDto paramsDto) {
        EventFilterParams params;
        try {
            String startString = paramsDto.getRangeStart();
            String endString = paramsDto.getRangeEnd();
            LocalDateTime start;
            LocalDateTime end = null;
            if (Objects.nonNull(startString)) {
                start = EWMTimeDecoderUrl.urlStringToLocalDateTime(startString);
            } else {
                start = LocalDateTime.now();
            }
            if (Objects.nonNull(endString)) {
                end = EWMTimeDecoderUrl.urlStringToLocalDateTime(endString);
                if (end.isBefore(start)) {
                    throw new EWMConflictException("Некорретный запрос.");
                }
            }
            params = EventFilterParams.builder()
                    .ids(paramsDto.getIds())
                    .states(paramsDto.getStates())
                    .categories(paramsDto.getCategories())
                    .rangeStart(start)
                    .rangeEnd(end)
                    .from(paramsDto.getFrom())
                    .size(paramsDto.getSize())
                    .onlyAvailable(paramsDto.getOnlyAvailable())
                    .text(paramsDto.getText())
                    .paid(paramsDto.getPaid())
                    .sort(paramsDto.getSort())
                    .build();
        } catch (UnsupportedEncodingException e) {
            throw new EWMConflictException("Некорретный запрос.");
        }
        return params;
    }

    private void publishEvent(UpdateEventAdminRequest request, Event event) {
        if(event.getState() != EventState.PENDING) {
            throw new EWMUpdateForbiddenException("Событие не может быть опубликовано.");
        } else {
            updateEventFields(request, event);
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }
    }

    private void rejectEvent(Event event) {
        if (event.getState() == EventState.PENDING) {
            event.setState(EventState.REJECTED);
        } else {
            throw new EWMUpdateForbiddenException("Событие не может быть отклонено.");
        }
    }

    private void confirmRequests(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result, Event event) {
        long confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        long limit = event.getParticipantLimit();
        boolean limitExceeded = false;
        for (ParticipationRequest request : requestsToUpdate) {
            checkPendingRequestStatus(request);
            if (confirmed == limit) {
                addToRejectedAndSave(request, result);
                limitExceeded = true;
            } else {
                addToConfirmedAndSave(request, result);
                confirmed++;
            }
        }
        if (limitExceeded) {
            throw new EWMRequestConfirmForbiddenException("Достигнут лимит участников в событии.");
        }
    }

    private void checkPendingRequestStatus(ParticipationRequest request) {
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new EWMRequestConfirmForbiddenException("Ошибка статуса запроса.");
        }
    }

    private void rejectRequests(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result, Event event) {
        for (ParticipationRequest request : requestsToUpdate) {
            checkPendingRequestStatus(request);
            addToRejectedAndSave(request, result);
        }
    }

    private void addToRejectedAndSave(ParticipationRequest request, EventRequestStatusUpdateResult result) {
        result.getRejectedRequests()
                .add(participationRequestMapper.participationRequestToParticipationRequestDto(request));
        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);
    }

    private void addToConfirmedAndSave(ParticipationRequest request, EventRequestStatusUpdateResult result) {
        result.getConfirmedRequests()
                .add(participationRequestMapper.participationRequestToParticipationRequestDto(request));
        request.setStatus(RequestStatus.CONFIRMED);
        requestRepository.save(request);
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
        Long views = statService.getViews(eventId);
        eventShortDto.setConfirmedRequests(confirmedRequests);
        eventShortDto.setViews(views);
        return eventShortDto;
    }

    private Event completeNewEvent(NewEventDto newEventDto, Long userId) {
        Event event = eventMapper.newEventDtoToEvent(newEventDto);
        User user = getUserIfExists(userId);
        Category category = getCategoryIfExists(newEventDto.getCategory()); // TODO make joins with corresponding tables
        Location location = getLocation(newEventDto.getLocation());
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setState(EventState.PENDING);
        return event;
    }

    private EventFullDto completeEventFullDto(Event event) {
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event);
        Long confirmedRequests = getConfirmedRequests(event.getId());
        boolean published = event.getState() == EventState.PUBLISHED;
        if (published) {
            completeWithViews(eventFullDto);
        }
        eventFullDto.setConfirmedRequests(confirmedRequests);
        return eventFullDto;
    }

    private Long getConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private EventFullDto updateEvent(UpdateEventUserRequest request, Event event) {
        updateEventFields(request, event);
        updateEventStateAction(request.getStateAction(), event);
        return eventMapper.eventToEventFullDto(event);
    }

    private void updateEventFields(UpdateEventRequest request, Event event) {
        updateEventAnnotation(request.getAnnotation(), event);
        updateEventCategory(request.getCategory(), event);
        updateEventDescription(request.getDescription(), event);
        updateEventDate(request.getEventDate(), event);
        updateEventLocation(request.getLocation(), event);
        updateEventPaidStatus(request.getPaid(), event);
        updateEventParticipationLimit(request.getParticipantLimit(), event);
        updateEventRequestModeration(request.getRequestModeration(), event);
        updateEventTitle(request.getTitle(), event);
    }

    private void updateEventTitle(String title, Event event) {
        if (Objects.nonNull(title)) {
            event.setTitle(title);
        }
    }

    private void updateEventStateAction(EventStateAction action, Event event) {
        if (Objects.nonNull(action)) {;
            if (action == EventStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (action == EventStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }
    }

    private void updateEventRequestModeration(Boolean requestModeration, Event event) {
        if (Objects.nonNull(requestModeration)) {
            event.setRequestModeration(requestModeration);
        }
    }

    private void updateEventParticipationLimit(Long limit, Event event) {
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
            if(Objects.nonNull(updatedEventDate)) {
                checkEventDateIsAfterNow(updatedEventDate, 2);
                event.setEventDate(updatedEventDate);
            }
        }
    }

    private void checkEventDateIsAfterNow(LocalDateTime dateTime, Integer gapFromNowInHours) {
        LocalDateTime minEventDateTime = LocalDateTime.now().plusHours(gapFromNowInHours);
        if (dateTime.isBefore(minEventDateTime)) {
            throw new EWMConflictException("Некорректная дата события.");
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

    private void checkEventStateInList(Event event, List<EventState> states) {
        for (EventState state : states) {
            if (event.getState() == state) {
                return;
            }
        }
        throw new EWMConflictException("Некорректный статус события.");
    }
}