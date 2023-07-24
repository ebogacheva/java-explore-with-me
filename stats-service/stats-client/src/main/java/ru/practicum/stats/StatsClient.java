package ru.practicum.stats;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.exception.IncorrectTimeLimitException;
import ru.practicum.stats_dto.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ru.practicum.stats_dto.TimeStampConverter.mapToString;

@Service
public class StatsClient {

    private static final String INCORRECT_TIME_LIMIT_EXCEPTION_INFO =
            "Please check time limit params: start and end shouldn't be null, end should be after start.";
    private static final String START_END_PATH_PART = "?start={start}&end={end}";
    private static final String HIT_ENDPOINT = "/hit";
    private static final String GET_STATS_ENDPOINT = "/stats";
    private static final String URIS_PATH_PART = "&uris=";
    private static final String UNIQUE_PATH_PART = "&unique=";

    private final RestTemplate rest;

    private StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build();
    }

    public ResponseEntity<Object> addHit(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHit endpointHit = new EndpointHit(app, uri, ip, mapToString(timestamp));
        return makeAndSendRequest(HttpMethod.POST, HIT_ENDPOINT, null, endpointHit);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start,
                                           LocalDateTime end,
                                           @Nullable List<String> uris,
                                           @Nullable Boolean unique) {
        Map<String, Object> parameters = getTimeLimitParameters(start, end);
        String path = getPath(uris, unique);
        return makeAndSendRequest(HttpMethod.GET, path, parameters, null);
    }

    private Map<String, Object> getTimeLimitParameters(LocalDateTime start, LocalDateTime end) {
        if (Objects.isNull(start) || Objects.isNull(end) || end.isBefore(start)) {
            throw new IncorrectTimeLimitException(INCORRECT_TIME_LIMIT_EXCEPTION_INFO);
        }
        return Map.of(
                "start", mapToString(start),
                "end", mapToString(end)
        );
    }

    private String getPath(List<String> uris, Boolean unique) {
        StringBuilder path = new StringBuilder(GET_STATS_ENDPOINT);
        path.append(START_END_PATH_PART);
        if (Objects.nonNull(uris) && !uris.isEmpty()) {
            for (String uri : uris) {
                path.append(URIS_PATH_PART).append(uri);
            }
        }
        if (Objects.nonNull(unique)) {
            path.append(UNIQUE_PATH_PART).append(unique);
        }
        return path.toString();
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method,
                                                          String path,
                                                          @Nullable Map<String, Object> parameters,
                                                          @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());
        ResponseEntity<Object> statsServerResponse;
        try {
            if (parameters != null) {
                statsServerResponse = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                statsServerResponse = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareStatsResponse(statsServerResponse);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private static ResponseEntity<Object> prepareStatsResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());
        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }
        return responseBuilder.build();
    }
}
