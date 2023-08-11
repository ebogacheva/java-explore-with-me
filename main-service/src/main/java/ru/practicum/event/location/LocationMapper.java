package ru.practicum.event.location;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    Location toLocation(LocationDto locationDto);
}
