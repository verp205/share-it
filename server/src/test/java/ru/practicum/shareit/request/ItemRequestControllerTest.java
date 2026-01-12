package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.ShareItServerApplication;
import ru.practicum.shareit.server.request.ItemRequestController;
import ru.practicum.shareit.server.request.ItemRequestService;
import ru.practicum.shareit.server.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
@ContextConfiguration(classes = ShareItServerApplication.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        // Given
        Long userId = 1L;
        ru.practicum.shareit.server.request.dto.ItemRequestDto requestDto =
                new ru.practicum.shareit.server.request.dto.ItemRequestDto("Нужна дрель");

        ItemRequestResponseDto createdRequest = new ItemRequestResponseDto(
                1L,
                "Нужна дрель",
                userId,
                LocalDateTime.now(),
                List.of()
        );

        when(itemRequestService.createRequest(eq(userId),
                any(ru.practicum.shareit.server.request.dto.ItemRequestDto.class)))
                .thenReturn(createdRequest);

        // When & Then
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.requesterId").value(userId));
    }

    @Test
    void getUserRequests_shouldReturnRequestsList() throws Exception {
        // Given
        Long userId = 1L;

        ItemRequestResponseDto request1 = new ItemRequestResponseDto(
                1L,
                "Нужна дрель",
                userId,
                LocalDateTime.now(),
                List.of()
        );

        ItemRequestResponseDto request2 = new ItemRequestResponseDto(
                2L,
                "Нужен молоток",
                userId,
                LocalDateTime.now(),
                List.of()
        );

        when(itemRequestService.getUserRequests(userId))
                .thenReturn(List.of(request1, request2));

        // When & Then
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Нужен молоток"));
    }

    @Test
    void getAllRequests_shouldReturnAllRequests() throws Exception {
        // Given
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;

        ItemRequestResponseDto request = new ItemRequestResponseDto(
                1L,
                "Нужна отвертка",
                2L, // Другой пользователь
                LocalDateTime.now(),
                List.of()
        );

        when(itemRequestService.getAllRequests(userId, from, size))
                .thenReturn(List.of(request));

        // When & Then
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Нужна отвертка"))
                .andExpect(jsonPath("$[0].requesterId").value(2));
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        // Given
        Long userId = 1L;
        Long requestId = 1L;

        ItemRequestResponseDto request = new ItemRequestResponseDto(
                requestId,
                "Нужен шуруповерт",
                userId,
                LocalDateTime.now(),
                List.of()
        );

        when(itemRequestService.getRequestById(userId, requestId))
                .thenReturn(request);

        // When & Then
        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Нужен шуруповерт"))
                .andExpect(jsonPath("$.requesterId").value(userId));
    }
}