package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserShortDto;

import java.util.Comparator;

import static ru.practicum.utils.ExploreDateTimeFormatter.stringToLocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class EventDto implements Comparable<EventDto>{

    private Long id;
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    private String eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private Long views;

    @Override
    public int compareTo(EventDto other) {
        return this.id.compareTo(other.id);
    }

    public static final Comparator<EventDto> EVENT_DATE_COMPARATOR =
            Comparator.comparing((EventDto eventDto) -> stringToLocalDateTime(eventDto.eventDate))
                    .thenComparing(EventDto::getId);;

    public static final Comparator<EventDto> VIEWS_COMPARATOR =
            Comparator.comparing(EventDto::getViews)
                    .thenComparing(EventDto::getId);
}
