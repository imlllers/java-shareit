package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.shareit.comment.CommentDto;

import java.util.List;

@Data
public class ItemDto {
    private Long id;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 512)
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
