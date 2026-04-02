package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemRequestDto {
    private Long id;

    @NotBlank
    @Size(max = 512)
    private String description;

    private Long requestorId;

    private LocalDateTime created;
}
