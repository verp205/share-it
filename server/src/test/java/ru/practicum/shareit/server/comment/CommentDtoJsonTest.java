package ru.practicum.shareit.server.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.server.ShareItServerApplication;
import ru.practicum.shareit.server.comment.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItServerApplication.class)
class CommentDtoJsonTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    }

    @Test
    void shouldSerializeCommentDto() throws Exception {
        // Given
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        CommentDto dto = new CommentDto(1L, "Great item!", "John", created);

        // When
        String json = objectMapper.writeValueAsString(dto);

        // Then
        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"text\":\"Great item!\"");
        assertThat(json).contains("\"authorName\":\"John\"");
        assertThat(json).contains("\"created\":\"2024-01-01T10:00:00\"");
    }

    @Test
    void shouldDeserializeCommentDto() throws Exception {
        // Given
        String json = "{\"id\":1,\"text\":\"Great item!\",\"authorName\":\"John\",\"created\":\"2024-01-01T10:00:00\"}";

        // When
        CommentDto dto = objectMapper.readValue(json, CommentDto.class);

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Great item!");
        assertThat(dto.getAuthorName()).isEqualTo("John");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
    }
}
