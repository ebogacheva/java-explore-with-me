package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Statistics;
import ru.practicum.stats_dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Statistics, Long> {

    @Query("select new ru.practicum.stats_dto.ViewStats(s.app, s.uri, count(s)) " +
            "from Statistics s " +
            "where s.uri IN ?3 " +
            "and s.timestamp >= ?1 " +
            "and s.timestamp <= ?2 " +
            "group BY s.app, s.uri")
    List<ViewStats> countLimitedListInTimeLimit(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.stats_dto.ViewStats(s.app, s.uri, count(distinct s.uri)) " +
            "from Statistics s " +
            "where s.uri IN ?3 " +
            "and s.created >= ?1 " +
            "and s.created <= ?2 " +
            "group BY s.app, s.uri")
    List<ViewStats> countUniqueLimitedListInTimeLimit(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.stats_dto.ViewStats(s.app, s.uri, count(s)) " +
            "from Statistics s " +
            "where s.timestamp >= ?1 " +
            "and s.timestamp <= ?2 " +
            "group BY s.app, s.uri")
    List<ViewStats> countAllInTimeLimit(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.stats_dto.ViewStats(s.app, s.uri, count(distinct s.uri)) " +
            "from Statistics s " +
            "where s.timestamp >= ?1 " +
            "and s.timestamp <= ?2 " +
            "group BY s.app, s.uri")
    List<ViewStats> countUniqueAllInTimeLimit(LocalDateTime start, LocalDateTime end);

}
