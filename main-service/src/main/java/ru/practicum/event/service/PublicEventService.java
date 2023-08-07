package ru.practicum.event.service;

import ru.practicum.event.dto.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PublicEventService {

    List<EventShortDto> getByPublic(EventFilterParamsDto params, HttpServletRequest request);

    EventFullDto get(Long eventId, HttpServletRequest request);
}

