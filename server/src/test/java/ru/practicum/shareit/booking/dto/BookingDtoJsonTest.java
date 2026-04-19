package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {
    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void shouldSerializeBookingDto() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setItemId(10L);
        dto.setStart(LocalDateTime.of(2026, 4, 17, 10, 15));
        dto.setEnd(LocalDateTime.of(2026, 4, 18, 10, 15));
        dto.setStatus(Status.WAITING);

        assertThat(json.write(dto)).hasJsonPathNumberValue("$.id");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.start")
                .startsWith("2026-04-17T10:15");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.status")
                .isEqualTo("WAITING");
    }
}
