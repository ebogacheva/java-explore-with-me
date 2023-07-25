package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class ParticipationRequestDto {

    private Long id;
    private String created;
    private Long eventId;
    private Long requesterId;
    private String status;
}