package ru.practicum.stats_dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class ViewStats {

    private String app;
    private String uri;
    private Integer hits;
}
