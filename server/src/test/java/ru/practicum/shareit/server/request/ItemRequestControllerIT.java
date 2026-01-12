package ru.practicum.shareit.server.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.user.dto.UserDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemRequestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullRequestFlow() throws Exception {
        // Создаем пользователя
        UserDto user = new UserDto("Alice", "alice@example.com");
        String userJson = objectMapper.writeValueAsString(user);

        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(userResponse).get("id").asLong();

        // Создаем запрос вещи
        ItemRequestDto requestDto = new ItemRequestDto("Need a drill");
        String requestJson = objectMapper.writeValueAsString(requestDto);

        String requestResponse = mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andReturn().getResponse().getContentAsString();

        Long requestId = objectMapper.readTree(requestResponse).get("id").asLong();

        // Получаем запрос пользователя
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Need a drill"));

        // Получаем все запросы (другим пользователем)
        UserDto user2 = new UserDto("Bob", "bob@example.com");
        String user2Response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long user2Id = objectMapper.readTree(user2Response).get("id").asLong();

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", user2Id)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Need a drill"));

        // Получаем запрос по id
        mockMvc.perform(get("/requests/{id}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }
}
