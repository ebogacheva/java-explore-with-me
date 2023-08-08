package ru.practicum.enums;

import ru.practicum.exception.EWMElementNotFoundException;

import static ru.practicum.utils.ExploreConstantsAndStaticMethods.EVENT_STATE_ACTION_NOT_FOUND_EXCEPTION;

public enum EventStateAction {

    SEND_TO_REVIEW,
    CANCEL_REVIEW,
    PUBLISH_EVENT,
    REJECT_EVENT
}
