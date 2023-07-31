package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UpdateEventUserRequest extends UpdateEventRequest {

}
