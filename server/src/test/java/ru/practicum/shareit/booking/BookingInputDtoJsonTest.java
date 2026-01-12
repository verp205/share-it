package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.server.ShareItServerApplication;
import ru.practicum.shareit.server.booking.dto.BookingInputDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItServerApplication.class)
class BookingInputDtoJsonTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    }

    @Test
    void shouldSerializeBookingInputDto() throws Exception {
        // Given
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 10, 0);
        BookingInputDto dto = new BookingInputDto(start, end, 1L);

        // When
        String json = objectMapper.writeValueAsString(dto);

        // Then
        assertThat(json).contains("\"start\":\"2024-01-01T10:00:00\"");
        assertThat(json).contains("\"end\":\"2024-01-02T10:00:00\"");
        assertThat(json).contains("\"itemId\":1");
    }

    @Test
    void shouldDeserializeBookingInputDto() throws Exception {
        // Given
        String json = "{\"start\":\"2024-01-01T10:00:00\",\"end\":\"2024-01-02T10:00:00\",\"itemId\":1}";

        // When
        BookingInputDto dto = objectMapper.readValue(json, BookingInputDto.class);

        // Then
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 0));
        assertThat(dto.getItemId()).isEqualTo(1L);
    }

    @Test
    void shouldHandleNullValues() throws Exception {
        // Given
        String json = "{\"start\":null,\"end\":null,\"itemId\":null}";

        // When
        BookingInputDto dto = objectMapper.readValue(json, BookingInputDto.class);

        // Then
        assertThat(dto.getStart()).isNull();
        assertThat(dto.getEnd()).isNull();
        assertThat(dto.getItemId()).isNull();
    }
}
