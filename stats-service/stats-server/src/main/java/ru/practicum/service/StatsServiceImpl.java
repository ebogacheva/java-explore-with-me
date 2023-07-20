package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.IncorrectTimeLimitException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Statistics;
import ru.practicum.repository.StatsRepository;
import ru.practicum.stats_dto.EndpointHit;
import ru.practicum.stats_dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static ru.practicum.stats_dto.TimeStampConverter.mapToLocalDateTime;

@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    @Transactional
    public void add(EndpointHit endpointHit) {
        LocalDateTime timestamp = mapToLocalDateTime(endpointHit.getTimestamp());
        Statistics hit = statsMapper.endpointHitToStatistics(endpointHit, timestamp);
        statsRepository.save(hit);
    }

    @Override
    public List<ViewStats> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        checkTimeLimitParams(start, end);
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

    private static void checkTimeLimitParams(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new IncorrectTimeLimitException("Please check time limit params: end shouldn't be before start.");
        }
    }
}
