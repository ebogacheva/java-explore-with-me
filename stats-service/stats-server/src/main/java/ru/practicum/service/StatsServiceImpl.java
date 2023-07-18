package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Statistics;
import ru.practicum.repository.StatsRepository;
import ru.practicum.stats_dto.EndpointHit;
import ru.practicum.stats_dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    @Transactional
    public void add(EndpointHit endpointHit) {
        Statistics hit = statsMapper.endpointHitToStatistics(endpointHit);
        statsRepository.save(hit);
    }

    @Override
    public List<ViewStats> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (Objects.isNull(uris) || uris.isEmpty()) {
            if (unique) {
                return statsRepository.countUniqueAllInTimeLimit(start, end);
            } else {
                return statsRepository.countAllInTimeLimit(start, end);
            }
        } else {
            if (unique) {
                return statsRepository.countUniqueLimitedListInTimeLimit(start, end, uris);
            } else {
                return statsRepository.countLimitedListInTimeLimit(start, end, uris);
            }
        }
    }

}
