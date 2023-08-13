package ru.practicum.user.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserShortDto {

    private Integer id;
    private String name;
}
