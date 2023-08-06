package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import ru.practicum.user.mapper.UserMapper;

import ru.practicum.utils.EWMDateTimeFormatter;
import ru.practicum.utils.EWMTimeDecoderUrl;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class, LocationMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventDate", expression = "java(ru.practicum.utils.EWMDateTimeFormatter.stringToLocalDateTime(newEventDto.getEventDate()))")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    Event newEventDtoToEvent(NewEventDto newEventDto);

    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "createdOn", expression = "java(ru.practicum.utils.EWMDateTimeFormatter.localDateTimeToString(event.getEventDate()))")
    @Mapping(target = "eventDate", expression = "java(ru.practicum.utils.EWMDateTimeFormatter.localDateTimeToString(event.getEventDate()))")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "publishedOn", expression = "java(ru.practicum.utils.EWMDateTimeFormatter.localDateTimeToString(event.getPublishedOn()))")
    @Mapping(target = "views", ignore = true)
    EventFullDto eventToEventFullDto(Event event);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "eventDate", expression = "java(ru.practicum.utils.EWMDateTimeFormatter.localDateTimeToString(event.getEventDate()))")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "views", ignore = true)
    EventShortDto eventToEventShortDto(Event event);

    List<Event> pageToList(Page<Event> page);
}
