package ru.practicum.request.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds = List.of();
    @NotNull
    private String requestStatus;
}
