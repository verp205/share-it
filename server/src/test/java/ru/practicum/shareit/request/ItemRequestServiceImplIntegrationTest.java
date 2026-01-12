package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.ShareItServerApplication;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.ItemService;
import ru.practicum.shareit.server.request.ItemRequestService;
import ru.practicum.shareit.server.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ContextConfiguration(classes = ShareItServerApplication.class)
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private Long userId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        // Создаем пользователя
        UserDto userDto = new UserDto(null, "User", "user@email.com");
        UserDto user = userService.createUser(userDto);
        userId = user.getId();

        // Создаем другого пользователя
        UserDto otherUserDto = new UserDto(null, "Other User", "other@email.com");
        UserDto otherUser = userService.createUser(otherUserDto);
        otherUserId = otherUser.getId();
    }

    @Test
    void createRequest_withValidData_shouldCreateRequest() {
        // Given
        ru.practicum.shareit.server.request.dto.ItemRequestDto requestDto =
                new ru.practicum.shareit.server.request.dto.ItemRequestDto("Нужна дрель");

        // When
        ItemRequestResponseDto request = itemRequestService.createRequest(userId, requestDto);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getId()).isNotNull();
        assertThat(request.getDescription()).isEqualTo("Нужна дрель");
        assertThat(request.getRequesterId()).isEqualTo(userId);
        assertThat(request.getCreated()).isNotNull();
        assertThat(request.getItems()).isEmpty();
    }

    @Test
    void getUserRequests_whenUserHasRequests_shouldReturnRequests() {
        // Given - создаем запрос
        ru.practicum.shareit.server.request.dto.ItemRequestDto requestDto =
                new ru.practicum.shareit.server.request.dto.ItemRequestDto("Нужен молоток");
        itemRequestService.createRequest(userId, requestDto);

        // When
        List<ItemRequestResponseDto> requests = itemRequestService.getUserRequests(userId);

        // Then
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getDescription()).isEqualTo("Нужен молоток");
    }

    @Test
    void getRequestById_whenRequestNotFound_shouldThrowException() {
        // When & Then
        assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(userId, 999L));
    }
}