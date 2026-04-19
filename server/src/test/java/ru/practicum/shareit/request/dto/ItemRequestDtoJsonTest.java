package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeItemRequestDto() throws Exception {
        ItemAnswerDto itemAnswerDto = new ItemAnswerDto();
        itemAnswerDto.setId(10L);
        itemAnswerDto.setName("Drill");
        itemAnswerDto.setOwnerId(5L);

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("Need drill");
        itemRequestDto.setRequestorId(2L);
        itemRequestDto.setCreated(LocalDateTime.of(2026, 4, 15, 12, 30));
        itemRequestDto.setItems(List.of(itemAnswerDto));

        assertThat(json.write(itemRequestDto)).hasJsonPathNumberValue("$.id");
        assertThat(json.write(itemRequestDto)).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need drill");
        assertThat(json.write(itemRequestDto)).extractingJsonPathStringValue("$.created")
                .startsWith("2026-04-15T12:30");
        assertThat(json.write(itemRequestDto)).extractingJsonPathNumberValue("$.items[0].ownerId")
                .isEqualTo(5);
    }
}
