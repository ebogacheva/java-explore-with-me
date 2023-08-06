package ru.practicum.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.EWMElementNotFoundException;
import ru.practicum.stats.StatsClient;
import ru.practicum.stats_dto.ViewStats;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utils.EWMCommonConstants.EVENT_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class StatService {

    private final StatsClient statsClient;
    private final EventRepository eventRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${app.name}")
    private String app;

    public void addHit(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        LocalDateTime timestamp = LocalDateTime.now();

        statsClient.addHit(app, uri, ip, timestamp);
    }

    public Long getViews(Long eventId) {
        Event event = getEventIfExists(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            return 0L;
        }
        LocalDateTime start = event.getPublishedOn();
        LocalDateTime end = LocalDateTime.now();
        String uri = "/events/" + event.getId();
        ResponseEntity<Object> response = statsClient.getStats(start, end, List.of(uri), null);
        return extractViews(response);
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EWMElementNotFoundException(EVENT_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private Long extractViews(ResponseEntity<Object> response) {
        try {
            String responseValue = mapper.writeValueAsString(response.getBody());
            List<ViewStats> viewStats = mapper.readValue(responseValue, new TypeReference<>() {});
            return viewStats.isEmpty() ? 0 : viewStats.get(0).getHits();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}