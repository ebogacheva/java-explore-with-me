package ru.practicum.service;

import java.time.LocalDateTime;
import java.util.List;
import ru.practicum.stats_dto.ViewStats;
import ru.practicum.stats_dto.EndpointHit;

public interface StatsService {

    void add(EndpointHit endpointHit);

    List<ViewStats> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
