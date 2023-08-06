package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.utils.EWMDateTimeFormatter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class EventDto {

    private Long id;
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    private String eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private Long views;

    public static final Comparator<EventDto> EVENT_DATE_COMPARATOR = (o1, o2) -> {
        LocalDateTime eventDate1 = EWMDateTimeFormatter.stringToLocalDateTime(o1.eventDate);
        LocalDateTime eventDate2 = EWMDateTimeFormatter.stringToLocalDateTime(o2.eventDate);
        Objects.requireNonNull(eventDate1);
        Objects.requireNonNull(eventDate2);
        return eventDate2.compareTo(eventDate1);
    };

    public static final Comparator<EventDto> VIEWS_COMPARATOR = (o1, o2) -> {
        Long views1 = o1.getViews();
        Long views2 = o2.getViews();
        return views2.compareTo(views1);
    };
}
