package ru.practicum.request.model;

public enum RequestStatus {
    PENDING ("PENDING"),
    CONFIRMED ("CONFIRMED"),
    CANCELED ("CANCELED"),
    REJECTED ("REJECTED");

    private final String name;

    RequestStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
