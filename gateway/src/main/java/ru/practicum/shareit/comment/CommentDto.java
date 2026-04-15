package ru.practicum.shareit.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;

    @NotBlank
    @Size(max = 512)
    private String text;

    private String authorName;

    private LocalDateTime created;
}
