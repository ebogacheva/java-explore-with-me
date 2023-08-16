package ru.practicum.user.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserWithSubDto {

    private Integer id;
    private String name;
    private List<Long> subForEvents = new ArrayList<>();
    private List<Long> subForParticipation = new ArrayList<>();
    private List<Long> mySubForEvents = new ArrayList<>();
    private List<Long> mySubForParticipation = new ArrayList<>();
}
