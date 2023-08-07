package ru.practicum.event.service;

import ru.practicum.event.dto.EventFilterParamsDto;
import ru.practicum.event.dto.*;
import java.util.List;

public interface AdminEventService {

    List<EventFullDto> getByAdmin(EventFilterParamsDto params);

    EventFullDto update(UpdateEventAdminRequest request, Long eventId);
}

