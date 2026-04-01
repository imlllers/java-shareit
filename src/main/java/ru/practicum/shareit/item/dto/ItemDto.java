package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;

@Data
public class ItemDto {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Boolean available;

    private Long requestId;

    private BookingInfoDto lastBooking;

    private BookingInfoDto nextBooking;

    private List<CommentDto> comments;

    @Data
    public static class BookingInfoDto {
        private Long id;
        private Long bookerId;
    }
}
