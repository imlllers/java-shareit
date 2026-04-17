package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;

    private Long bookerId;

    private ItemInfo item;

    private BookerInfo booker;

    private Status status;

    @Data
    public static class ItemInfo {
        private Long id;
        private String name;
    }

    @Data
    public static class BookerInfo {
        private Long id;
    }
}
