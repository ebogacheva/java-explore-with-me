package ru.practicum.request.dto;

import lombok.*;
import ru.practicum.request.model.RequestStatus;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds;
    private String status;
}
