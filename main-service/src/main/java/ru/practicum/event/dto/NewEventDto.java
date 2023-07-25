package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class NewEventDto {

    @Length(min = 20, max = 2000)
    private String annotation;
    @NotNull
    private Long catId;
    @Length(min = 20, max = 7000)
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String eventDate;
    @NotNull
    private LocationDto locationDto;
    private Boolean paid;
    @Min(0)
    private Integer participantLimit;
    @Builder.Default
    private Boolean requestModeration = Boolean.TRUE;
    @Length(min = 3, max = 120)
    private String title;
}
