package ru.practicum.event.model;

import ru.practicum.exception.EWMElementNotFoundException;

import static ru.practicum.utils.EWMCommonConstants.EVENT_STATE_ACTION_NOT_FOUND_EXCEPTION;

public enum EventStateAction {
    SEND_TO_REVIEW ("SEND_TO_REVIEW"),
    CANCEL_REVIEW ("CANCEL_REVIEW"),
    PUBLISH_EVENT("PUBLISH_EVENT"),
    REJECT_EVENT("REJECT_EVENT");

    private final String name;

    EventStateAction(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static EventStateAction fromString(String name) { //TODO: revise for users?
        for (EventStateAction action : EventStateAction.values()) {
            if (action.name.equalsIgnoreCase(name)) {
                return action;
            }
        }
        throw new EWMElementNotFoundException(EVENT_STATE_ACTION_NOT_FOUND_EXCEPTION);
    }
}
