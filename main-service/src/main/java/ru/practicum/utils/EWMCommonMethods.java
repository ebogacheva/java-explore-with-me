package ru.practicum.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class EWMCommonMethods {

    public static Pageable pageRequestOf(int from, int size) {
        int page = from / size;
        return PageRequest.of(page, size);
    }

}
