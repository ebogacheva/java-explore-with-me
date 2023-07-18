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
    public List<ViewStats> getStats(@RequestParam(required = true) LocalDateTime start,
                                    @RequestParam(required = true) LocalDateTime end,
                                    @RequestParam(required = false) List<String> uri,
                                    @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        if (end.isBefore(start)) {
            throw new IncorrectTimeLimitException("Please check time limit params: end shouldn't be before start.");
        }
        return statsService.getStatistics(start, end, uri, unique);
    }
}
