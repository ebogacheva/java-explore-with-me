package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    List<Long> requestIds;
    @NotNull
    String requestStatus;
}
