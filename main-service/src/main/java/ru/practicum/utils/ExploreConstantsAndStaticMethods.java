package ru.practicum.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ExploreConstantsAndStaticMethods {

        /*----------Category String Utils------------*/
        public static final String CATEGORY_NAME_ALREADY_EXISTS_EXCEPTION = "Category name already exists.";
        public static final String CATEGORY_NOT_FOUND_EXCEPTION = "Category not found.";
        public static final String CATEGORY_IS_CONNECTED_WITH_EVENTS =
                "Category is connected with events and could be deleted.";

        /*----------User String Utils------------*/
        public static final String USER_NOT_FOUND_EXCEPTION_MESSAGE = "User not found or unavailable.";
        public static final String USER_NAME_ALREADY_EXISTS = "User name already exists and could be saved.";

        /*----------Event String Utils------------*/
        public static final String EVENT_NOT_FOUND_EXCEPTION = "Event not found.";
        public static final String EVENT_STATE_ACTION_NOT_FOUND_EXCEPTION = "Event-state-action not found.";

        /*----------Compilation String Utils------------*/
        public static final String EVENTS_FROM_COMPILATION_NOT_FOUND = "Some events from the compilation not found.";
        public static final String COMPILATION_NOT_FOUND = "Compilation not found.";
        public static final String COMPILATION_TITLE_ALREADY_EXIST = "Compilation title already exists and could not be used";

        public static Pageable pageRequestOf(int from, int size) {
                int page = from / size;
                return PageRequest.of(page, size);
        }


}