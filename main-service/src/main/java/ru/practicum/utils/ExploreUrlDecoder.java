package ru.practicum.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static ru.practicum.utils.ExploreDateTimeFormatter.stringToLocalDateTime;

public class ExploreUrlDecoder {

    public static LocalDateTime urlStringToLocalDateTime(String input) throws UnsupportedEncodingException {
        String decodedDateTimeStr = java.net.URLDecoder.decode(input, StandardCharsets.UTF_8);
        return stringToLocalDateTime(decodedDateTimeStr);
    }
}


