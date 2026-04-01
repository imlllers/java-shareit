package ru.practicum.shareit.booking.model;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        return BookingState.valueOf(value.toUpperCase());
    }
}
