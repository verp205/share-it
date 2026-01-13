package ru.practicum.shareit.server.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.comment.dto.CommentDto;
import ru.practicum.shareit.server.item.dto.ItemRequestDto;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem() throws Exception {
        when(itemService.createItem(eq(1L), any()))
                .thenReturn(new ItemResponseDto());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemRequestDto())))
                .andExpect(status().isCreated());
    }

    @Test
    void updateItem() throws Exception {
        when(itemService.updateItem(eq(1L), eq(1L), any()))
                .thenReturn(new ItemResponseDto());

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void getItem() throws Exception {
        when(itemService.getItem(1L, 1L))
                .thenReturn(new ItemResponseDto());

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getItemsOfOwner() throws Exception {
        when(itemService.getItemsOfOwner(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems() throws Exception {
        when(itemService.search("text"))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", "text"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment() throws Exception {
        when(itemService.addComment(eq(1L), eq(1L), any()))
                .thenReturn(new CommentDto());

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
    }
}
