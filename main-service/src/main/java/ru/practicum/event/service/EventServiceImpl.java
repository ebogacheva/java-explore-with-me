package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.EventFilterParamsDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.*;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.*;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.utils.EWMCommonConstants.*;
import static ru.practicum.utils.EWMCommonMethods.pageRequestOf;
import static ru.practicum.utils.EWMDateTimeFormatter.stringToLocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements AdminEventService, PublicEventService, PrivateEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final ParticipationRequestMapper participationRequestMapper;

    private final StatService statService;

    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        getUserIfExists(userId);
        Page<Event> pageOfEvents = eventRepository.findByInitiatorId(userId, pageRequestOf(from, size));
        return eventMapper.pageToList(pageOfEvents)
                .stream()
                .map(eventMapper::eventToEventShortDto)
                .map(this::completeEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto create(NewEventDto newEventDto, Long userId) {
        Event newEvent = completeNewEvent(newEventDto, userId);
        checkEventDateIsAfterNow(newEvent.getEventDate(), 2);
        Event savedEvent = eventRepository.save(newEvent);
        return completeEventFullDto(savedEvent);
    }

    @Override
    public EventFullDto getEvent(Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        return completeEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto update(UpdateEventUserRequest request, Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
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
                .map(participationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult update(EventRequestStatusUpdateRequest update, Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        List<Long> ids = update.getRequestIds();

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        if (!event.getRequestModeration() ||
                event.getParticipantLimit() == 0 ||
                update.getRequestIds().isEmpty()) {
            return result;
        }
        List<ParticipationRequest> requestsToUpdate = requestRepository.findAllByIdIn(ids);
        checkAllRequestsPending(requestsToUpdate);
        RequestStatus status = RequestStatus.valueOf(update.getStatus());
        if (status == RequestStatus.CONFIRMED) {
            confirmRequests(requestsToUpdate, result, event);
        } else if (status == RequestStatus.REJECTED) {
            rejectRequests(requestsToUpdate, result);
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
                .map(eventDto -> (EventFullDto) eventDto)
                .sorted(getComparator(params.getSort()).reversed())
                .collect(Collectors.toList());
        return events;
    }

    private static void checkAllRequestsPending(List<ParticipationRequest> requests) {
        Optional<ParticipationRequest> notPending = requests.stream()
                .filter(request -> request.getStatus() != RequestStatus.PENDING)
                .findAny();
        if (notPending.isPresent()) {
            throw new EWMConflictException("Статус запроса изменить невозможно");
        }
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
    @Transactional
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
        List<EventShortDto> events = eventRepository.publicEventsSearch(params)
                .stream()
                .map(eventMapper::eventToEventShortDto)
                .peek(this::completeWithRequests)
                .peek(this::completeWithViews)
                .sorted(getComparator(params.getSort()).reversed())
                .collect(Collectors.toList());
        statService.addHit(request);
        return events;
    }

    private Comparator<EventDto> getComparator(EventSort sortType) {
        if (sortType != null) {
            if (sortType == EventSort.VIEWS) {
                return EventDto.VIEWS_COMPARATOR;
            }
        }
        return EventDto.EVENT_DATE_COMPARATOR;

    }

    @Override
    public EventFullDto get(Long eventId, HttpServletRequest request) {
        Event event = getEventIfExists(eventId);
        checkEventIsPublished(event);
        EventFullDto eventFullDto = completeEventFullDto(event);
        statService.addHit(request);
        return eventFullDto;
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
                    throw new EWMIncorrectParamsException("Некорретный запрос.");
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
            throw new EWMConflictException("Событие не может быть опубликовано.");
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
            throw new EWMConflictException("Событие не может быть отклонено.");
        }
    }

    private void confirmRequests(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result, Event event) {
        long confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        long limit = event.getParticipantLimit();

        for (ParticipationRequest request : requestsToUpdate) {
            if (confirmed == limit) {
                int start = requestsToUpdate.indexOf(request);
                int end = requestsToUpdate.size();
                rejectRequests(requestsToUpdate.subList(start, end), result);
                throw new EWMConflictException("Достигнут лимит участников в событии.");
            }
            confirmRequests(List.of(request), result);
            confirmed++;
        }
    }

    private void rejectRequests(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result) {
        requestsToUpdate.forEach(r -> r.setStatus(RequestStatus.REJECTED));
        List<ParticipationRequest> rejected = requestRepository.saveAll(requestsToUpdate);
        result.setRejectedRequests(mapToRequestDtoList(rejected));
    }

    private List<ParticipationRequestDto> mapToRequestDtoList(List<ParticipationRequest> requests) {
//        return requests.stream()
//                .map(participationRequestMapper::toParticipationRequestDto)
//                .collect(Collectors.toList());
        return participationRequestMapper.toParticipationRequestDtoList(requests);
    }

    private void confirmRequests(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result) {
        requestsToUpdate.forEach(r -> r.setStatus(RequestStatus.CONFIRMED));
        List<ParticipationRequest> confirmed = requestRepository.saveAll(requestsToUpdate);
        result.setConfirmedRequests(mapToRequestDtoList(confirmed));
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EWMElementNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EWMElementNotFoundException(EVENT_NOT_FOUND_EXCEPTION));
    }

    private Category getCategoryIfExists(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new EWMElementNotFoundException(CATEGORY_NOT_FOUND_EXCEPTION));
    }

    private Location getLocation(LocationDto locationDto) {
        Location location = locationMapper.locationDtoToLocation(locationDto);
        return locationRepository.getByLatAndLon(location.getLat(), location.getLon())
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
        completeWithViews(eventFullDto);
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
                checkEventDateIsAfterNow(updatedEventDate, 1);
                event.setEventDate(updatedEventDate);
            }
        }
    }

    private void checkEventDateIsAfterNow(LocalDateTime dateTime, Integer gapFromNowInHours) {
        LocalDateTime minEventDateTime = LocalDateTime.now().plusHours(gapFromNowInHours);
        if (dateTime.isBefore(minEventDateTime)) {
            throw new EWMIncorrectParamsException("Некорректная дата события.");
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
        if (!states.contains(event.getState())) {
            throw new EWMConflictException("Некорректный статус события.");
        }
    }
}