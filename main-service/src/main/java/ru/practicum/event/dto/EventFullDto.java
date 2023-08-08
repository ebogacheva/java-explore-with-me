package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.event.location.LocationDto;

@Getter
@Setter
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class EventFullDto extends EventDto{

    private String createdOn;
    private String description;
    private LocationDto location;
    private Integer participantLimit;
    private String publishedOn;
    private Boolean requestModeration;
    private String state;

}
