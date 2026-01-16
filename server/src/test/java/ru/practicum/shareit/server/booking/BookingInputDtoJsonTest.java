package ru.practicum.shareit.server.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.server.booking.dto.BookingInputDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingInputDtoJsonTest {

    @Autowired
    private JacksonTester<BookingInputDto> json;

    @Test
    void serializeBookingInputDto() throws Exception {
        BookingInputDto dto = new BookingInputDto(
                LocalDateTime.of(2025, 2, 1, 10, 0),
                LocalDateTime.of(2025, 2, 2, 10, 0),
                5L
        );

        String content = json.write(dto).getJson();

        assertThat(content).contains("\"itemId\":5");
        assertThat(content).contains("\"start\":\"2025-02-01T10:00:00\"");
        assertThat(content).contains("\"end\":\"2025-02-02T10:00:00\"");
    }
}
