package ru.practicum.stats_dto;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ViewStats {

    private String app;
    private String uri;
    private Long hits;
}
