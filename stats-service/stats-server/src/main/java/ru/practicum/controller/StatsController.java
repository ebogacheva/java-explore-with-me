package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.exception.IncorrectTimeLimitException;
import ru.practicum.service.StatsServiceImpl;
import ru.practicum.stats_dto.EndpointHit;
import ru.practicum.stats_dto.ViewStats;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.stats_dto.TimeStampConverter.mapToLocalDateTime;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class StatsController {

    private final StatsServiceImpl statsService;

    @PostMapping(path = "/hit")
    public void add(@Valid @RequestBody EndpointHit endpointHit) {
        statsService.add(endpointHit);
    }

    @GetMapping(path = "/stats")
    public List<ViewStats> getStats(@RequestParam(value = "start") String startStr,
                                    @RequestParam(value = "end") String endStr,
                                    @RequestParam(required = false, defaultValue = "") List<String> uris,
                                    @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        LocalDateTime start = mapToLocalDateTime(startStr);
        LocalDateTime end = mapToLocalDateTime(endStr);
        if (end.isBefore(start)) {
            throw new IncorrectTimeLimitException("Please check time limit params: end shouldn't be before start.");
        }
        return statsService.getStatistics(start, end, uris, unique);
    }
}
