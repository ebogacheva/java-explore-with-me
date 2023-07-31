package ru.practicum.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
public class EWMTimeDecoderUrl {

    public static LocalDateTime urlStringToLocalDateTime(String input) throws UnsupportedEncodingException {
        String decodedDateTimeStr = java.net.URLDecoder.decode(input, StandardCharsets.UTF_8);
        return EWMDateTimeFormatter.stringToLocalDateTime(decodedDateTimeStr);
    }
}


