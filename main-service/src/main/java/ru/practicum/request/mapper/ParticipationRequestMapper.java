package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;

import ru.practicum.utils.EWMDateTimeFormatter;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {
    @Mapping(target = "eventId", expression = "java(request.getEvent().getId())")
    @Mapping(target = "created", expression = "java(ru.practicum.utils.EWMDateTimeFormatter.localDateTimeToString(request.getCreated()))")
    @Mapping(target = "requesterId", expression = "java(request.getRequester().getId())")
    @Mapping(target = "status", expression = "java(request.getStatus().getName())")
    ParticipationRequestDto participationRequestToParticipationRequestDto(ParticipationRequest request);
}
