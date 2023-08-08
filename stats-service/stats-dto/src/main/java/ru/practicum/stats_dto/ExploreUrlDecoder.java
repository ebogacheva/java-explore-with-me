package ru.practicum.stats_dto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static ru.practicum.stats_dto.TimeStampConverter.mapToLocalDateTime;

public class ExploreUrlDecoder {

    public static LocalDateTime urlStringToLocalDateTime(String input) {
        String decodedDateTimeStr = java.net.URLDecoder.decode(input, StandardCharsets.UTF_8);
        return mapToLocalDateTime(decodedDateTimeStr);
    }
}


