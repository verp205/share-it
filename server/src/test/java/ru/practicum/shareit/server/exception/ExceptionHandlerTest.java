package ru.practicum.shareit.server.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.ShareItServerApplication;
import ru.practicum.shareit.server.error.ErrorHandler;
import ru.practicum.shareit.server.user.UserController;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {UserController.class, ErrorHandler.class})
@ContextConfiguration(classes = ShareItServerApplication.class)
class ExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void handleNotFoundException_shouldReturn404() throws Exception {
        // Given
        when(userService.getUser(999L)).thenThrow(new NotFoundException("Пользователь", 999L));

        // When & Then
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createUser_shouldReturn200_evenIfDtoInvalid() throws Exception {
        UserDto invalidUser = new UserDto(null, "", "invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isOk());
    }

    @Test
    void handleConflictException_shouldReturn409() throws Exception {
        // Given
        UserDto userDto = new UserDto(null, "User", "duplicate@email.com");
        when(userService.createUser(any(UserDto.class)))
                .thenThrow(new ConflictException("Пользователь с таким email уже существует"));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void handleForbiddenException_shouldReturn403() throws Exception {
        // Given
        when(userService.getUser(1L))
                .thenThrow(new ForbiddenException("доступ запрещен"));

        // When & Then
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }
}
