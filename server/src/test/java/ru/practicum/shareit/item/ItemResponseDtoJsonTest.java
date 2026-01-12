package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.server.ShareItServerApplication;
import ru.practicum.shareit.server.booking.dto.BookingShortDto;
import ru.practicum.shareit.server.comment.dto.CommentDto;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItServerApplication.class)
class ItemResponseDtoJsonTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldSerializeItemResponseDto() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        BookingShortDto lastBooking = new BookingShortDto(1L, 2L, now.minusDays(2), now.minusDays(1));
        BookingShortDto nextBooking = new BookingShortDto(2L, 3L, now.plusDays(1), now.plusDays(2));
        CommentDto comment = new CommentDto(1L, "Great item!", "John", now);

        ItemResponseDto dto = new ItemResponseDto(
                1L,
                "Дрель",
                "Мощная дрель",
                true,
                10L,
                5L,
                lastBooking,
                nextBooking,
                List.of(comment)
        );

        // When
        String json = objectMapper.writeValueAsString(dto);

        // Then
        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"ownerId\":10");
        assertThat(json).contains("\"requestId\":5");
        assertThat(json).contains("\"lastBooking\"");
        assertThat(json).contains("\"nextBooking\"");
        assertThat(json).contains("\"comments\"");
    }

    @Test
    void shouldDeserializeItemResponseDto() throws Exception {
        // Given
        String json = "{\"id\":1,\"name\":\"Дрель\",\"description\":\"Мощная дрель\"," +
                "\"available\":true,\"ownerId\":10,\"requestId\":5," +
                "\"lastBooking\":{\"id\":1,\"bookerId\":2,\"start\":\"2024-01-01T10:00:00\",\"end\":\"2024-01-02T10:00:00\"}," +
                "\"nextBooking\":{\"id\":2,\"bookerId\":3,\"start\":\"2024-01-03T10:00:00\",\"end\":\"2024-01-04T10:00:00\"}," +
                "\"comments\":[{\"id\":1,\"text\":\"Great item!\",\"authorName\":\"John\",\"created\":\"2024-01-05T10:00:00\"}]}";

        // When
        ItemResponseDto dto = objectMapper.readValue(json, ItemResponseDto.class);

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getOwnerId()).isEqualTo(10L);
        assertThat(dto.getRequestId()).isEqualTo(5L);
        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getNextBooking()).isNotNull();
        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getText()).isEqualTo("Great item!");
    }

    @Test
    void shouldHandleMissingFields() throws Exception {
        // Given
        String json = "{\"id\":1,\"name\":\"Дрель\"}";

        // When
        ItemResponseDto dto = objectMapper.readValue(json, ItemResponseDto.class);

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getAvailable()).isNull();
        assertThat(dto.getOwnerId()).isNull();
        assertThat(dto.getRequestId()).isNull();
        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
        assertThat(dto.getComments()).isNull();
    }
}