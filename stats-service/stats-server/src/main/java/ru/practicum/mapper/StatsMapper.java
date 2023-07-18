package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.model.Statistics;
import ru.practicum.stats_dto.EndpointHit;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    @Mapping(target = "created",
            expression = "java(timestamp)")
    Statistics endpointHitToStatistics(EndpointHit endpointHit, LocalDateTime timestamp);
}


