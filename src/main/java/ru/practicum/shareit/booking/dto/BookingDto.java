package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;

    @NotNull
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;

    @NotNull
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
