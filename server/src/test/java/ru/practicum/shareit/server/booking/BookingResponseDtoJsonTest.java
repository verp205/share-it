package ru.practicum.shareit.server.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;
import ru.practicum.shareit.server.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingResponseDtoJsonTest {

    @Autowired
    private JacksonTester<BookingResponseDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeBookingResponseDto() throws Exception {
        BookingResponseDto dto = new BookingResponseDto(
                1L,
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 11, 12, 0),
                BookingStatus.APPROVED,
                new UserDto(2L, "User", "user@mail.com"),
                new ItemResponseDto(
                        3L,
                        "Item",
                        "Desc",
                        true,
                        4L,
                        null,
                        null,
                        null,
                        null
                )
        );

        String content = json.write(dto).getJson();

        assertThat(content).contains("\"id\":1");
        assertThat(content).contains("\"status\":\"APPROVED\"");
        assertThat(content).contains("\"start\":\"2025-01-10T12:00:00\"");
        assertThat(content).contains("\"booker\"");
        assertThat(content).contains("\"item\"");
    }

    @Test
    void deserializeBookingResponseDto() throws Exception {
        String jsonContent = "{"
                + "\"id\":1,"
                + "\"start\":\"2025-01-10T12:00:00\","
                + "\"end\":\"2025-01-11T12:00:00\","
                + "\"status\":\"WAITING\","
                + "\"booker\":{"
                +     "\"id\":2,"
                +     "\"name\":\"User\","
                +     "\"email\":\"user@mail.com\""
                + "},"
                + "\"item\":{"
                +     "\"id\":3,"
                +     "\"name\":\"Item\","
                +     "\"description\":\"Desc\","
                +     "\"available\":true,"
                +     "\"ownerId\":4"
                + "}"
                + "}";

        BookingResponseDto dto =
                objectMapper.readValue(jsonContent, BookingResponseDto.class);

        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
        assertThat(dto.getItem().getOwnerId()).isEqualTo(4L);
    }
}
