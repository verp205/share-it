package ru.practicum.shareit.server.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.server.request.dto.RequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestResponseDtoJsonTest {

    @Autowired
    private JacksonTester<RequestResponseDto> json;

    @Test
    void serializeItemRequestResponseDto() throws Exception {
        RequestResponseDto dto = new RequestResponseDto(
                1L,
                "Need drill",
                2L,
                LocalDateTime.of(2025, 3, 1, 12, 0),
                List.of()
        );

        String content = json.write(dto).getJson();

        assertThat(content).contains("\"id\":1");
        assertThat(content).contains("\"description\":\"Need drill\"");
        assertThat(content).contains("\"created\":\"2025-03-01T12:00:00\"");
        assertThat(content).contains("\"items\"");
    }
}
