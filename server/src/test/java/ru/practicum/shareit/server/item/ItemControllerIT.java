package ru.practicum.shareit.server.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.item.dto.ItemRequestDto;
import ru.practicum.shareit.server.user.dto.UserDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullItemFlow() throws Exception {
        // Создаем пользователя
        UserDto user = new UserDto("Alice", "alice@example.com");
        String userJson = objectMapper.writeValueAsString(user);

        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long ownerId = objectMapper.readTree(userResponse).get("id").asLong();

        // Создаем предмет
        ItemRequestDto itemDto = new ItemRequestDto(null, "Drill", "Power drill", true, null);
        String itemJson = objectMapper.writeValueAsString(itemDto);

        String itemResponse = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Drill"))
                .andReturn().getResponse().getContentAsString();

        Long itemId = objectMapper.readTree(itemResponse).get("id").asLong();

        // Получаем предмет по id
        mockMvc.perform(get("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill"));

        // Обновляем предмет
        itemDto.setName("Updated Drill");
        mockMvc.perform(patch("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Drill"));

        // Поиск предметов
        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Updated Drill"));
    }
}
