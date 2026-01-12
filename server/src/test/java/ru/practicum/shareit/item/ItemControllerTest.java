package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.ShareItServerApplication;
import ru.practicum.shareit.server.comment.dto.CommentDto;
import ru.practicum.shareit.server.item.ItemController;
import ru.practicum.shareit.server.item.ItemService;
import ru.practicum.shareit.server.item.dto.ItemRequestDto;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@ContextConfiguration(classes = ShareItServerApplication.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        // Given
        Long ownerId = 1L;
        ItemRequestDto itemDto = new ItemRequestDto(null, "Дрель", "Мощная дрель", true, null);
        ItemResponseDto createdItem = new ItemResponseDto(1L, "Дрель", "Мощная дрель",
                true, ownerId, null, null, null, List.of());

        when(itemService.createItem(eq(ownerId), any(ItemRequestDto.class))).thenReturn(createdItem);

        // When & Then
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.ownerId").value(ownerId));
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        // Given
        Long ownerId = 1L;
        Long itemId = 1L;
        ItemRequestDto updateDto = new ItemRequestDto(null, "Дрель Updated", "Обновленное описание", true, null);
        ItemResponseDto updatedItem = new ItemResponseDto(itemId, "Дрель Updated",
                "Обновленное описание", true, ownerId, null, null, null, List.of());

        when(itemService.updateItem(eq(ownerId), eq(itemId), any(ItemRequestDto.class)))
                .thenReturn(updatedItem);

        // When & Then
        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Дрель Updated"));
    }

    @Test
    void getItem_shouldReturnItem() throws Exception {
        // Given
        Long itemId = 1L;
        Long userId = 1L;
        ItemResponseDto item = new ItemResponseDto(itemId, "Дрель", "Описание",
                true, userId, null, null, null, List.of());

        when(itemService.getItem(eq(itemId), anyLong())).thenReturn(item);

        // When & Then
        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getItem_withoutUserId_shouldReturnItem() throws Exception {
        // Given
        Long itemId = 1L;
        ItemResponseDto item = new ItemResponseDto(itemId, "Дрель", "Описание",
                true, 1L, null, null, null, List.of());

        when(itemService.getItem(eq(itemId), isNull())).thenReturn(item);

        // When & Then
        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId));
    }

    @Test
    void getItemsOfOwner_shouldReturnItemsList() throws Exception {
        // Given
        Long ownerId = 1L;
        ItemResponseDto item1 = new ItemResponseDto(1L, "Дрель", "Описание1",
                true, ownerId, null, null, null, List.of());
        ItemResponseDto item2 = new ItemResponseDto(2L, "Молоток", "Описание2",
                true, ownerId, null, null, null, List.of());

        when(itemService.getItemsOfOwner(ownerId)).thenReturn(List.of(item1, item2));

        // When & Then
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Молоток"));
    }

    @Test
    void searchItems_shouldReturnMatchingItems() throws Exception {
        // Given
        String searchText = "дрель";
        ItemResponseDto item = new ItemResponseDto(1L, "Дрель", "Мощная дрель",
                true, 1L, null, null, null, List.of());

        when(itemService.search(searchText)).thenReturn(List.of(item));

        // When & Then
        mockMvc.perform(get("/items/search")
                        .param("text", searchText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        // Given
        Long userId = 1L;
        Long itemId = 1L;
        CommentDto commentDto = new CommentDto(null, "Отличная вещь!", null, null);
        CommentDto createdComment = new CommentDto(1L, "Отличная вещь!", "User",
                LocalDateTime.now());

        when(itemService.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenReturn(createdComment);

        // When & Then
        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Отличная вещь!"));
    }
}