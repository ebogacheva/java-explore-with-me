package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.model.Statistics;
import ru.practicum.stats_dto.EndpointHit;
import ru.practicum.stats_dto.TimeStampConverter;

@Mapper(componentModel = "spring")
@RequiredArgsConstructor
public abstract class StatsMapper {

    @Autowired
    public TimeStampConverter timeStampConverter;

    @Mapping(target = "created",
            expression = "java(TimeStampConverter.mapStringToLocalDateTime(source.getTimestamp()))")
    public abstract Statistics endpointHitToStatistics(EndpointHit endpointHit);
}


