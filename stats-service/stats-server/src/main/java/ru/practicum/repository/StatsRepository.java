package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Statistics;
import ru.practicum.stats_dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Statistics, Long> {

    @Query("select new ru.practicum.stats_dto.ViewStats(s.app, s.uri, count(s.ip)) " +
            "from statistics s " +
            "where s.uri IN ?3 " +
            "and s.created between ?1 and ?2 " +
            "group BY s.app, s.uri " +
            "order by count(s.ip) desc")
    List<ViewStats> countLimitedListInTimeLimit(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.stats_dto.ViewStats(s.app, s.uri, count(distinct s.ip)) " +
            "from statistics s " +
            "where s.uri IN ?3 " +
            "and s.created between ?1 and ?2 " +
            "group BY s.app, s.uri " +
            "order by count(s.ip) desc")
    List<ViewStats> countUniqueLimitedListInTimeLimit(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.stats_dto.ViewStats(s.app, s.uri, count(s.ip)) " +
            "from statistics s " +
            "where s.created between ?1 and ?2 " +
            "group BY s.app, s.uri  " +
            "order by count(s.ip) desc")
    List<ViewStats> countAllInTimeLimit(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.stats_dto.ViewStats(s.app, s.uri, count(distinct s.ip)) " +
            "from statistics s " +
            "where s.created between ?1 and ?2 " +
            "group BY s.app, s.uri " +
            "order by count(s.ip) desc")
    List<ViewStats> countUniqueAllInTimeLimit(LocalDateTime start, LocalDateTime end);

}
