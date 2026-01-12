package ru.practicum.shareit.server.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User user;
    private ItemRequestDto requestDto;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Alice", "alice@example.com");
        requestDto = new ItemRequestDto("Need a drill");
        request = new ItemRequest(1L, "Need a drill", user, LocalDateTime.now(), List.of());
    }

    @Test
    void createRequest_ShouldReturnResponseDto() {
        when(userService.getUserEntity(1L)).thenReturn(user);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);

        ItemRequestResponseDto result = requestService.createRequest(1L, requestDto);

        assertNotNull(result);
        assertEquals("Need a drill", result.getDescription());
        assertEquals(user.getId(), result.getRequesterId());
    }

    @Test
    void getUserRequests_ShouldReturnList() {
        when(userService.getUserEntity(1L)).thenReturn(user);
        when(requestRepository.findByRequesterIdOrderByCreatedDesc(1L)).thenReturn(List.of(request));

        List<ItemRequestResponseDto> result = requestService.getUserRequests(1L);

        assertEquals(1, result.size());
        assertEquals("Need a drill", result.get(0).getDescription());
    }

    @Test
    void getAllRequests_ShouldReturnList() {
        User otherUser = new User(2L, "Bob", "bob@example.com");
        ItemRequest request2 = new ItemRequest(2L, "Need hammer", otherUser, LocalDateTime.now(), List.of());

        when(userService.getUserEntity(1L)).thenReturn(user);
        when(requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(eq(1L), any()))
                .thenReturn(List.of(request2));

        List<ItemRequestResponseDto> result = requestService.getAllRequests(1L, 0, 10);

        assertEquals(1, result.size());
        assertEquals("Need hammer", result.get(0).getDescription());
    }

    @Test
    void getRequestById_WhenFound_ShouldReturnDto() {
        when(userService.getUserEntity(1L)).thenReturn(user);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        ItemRequestResponseDto result = requestService.getRequestById(1L, 1L);

        assertEquals("Need a drill", result.getDescription());
        assertEquals(user.getId(), result.getRequesterId());
    }

    @Test
    void getRequestById_WhenNotFound_ShouldThrowNotFoundException() {
        when(userService.getUserEntity(1L)).thenReturn(user);
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getRequestById(1L, 1L));
    }
}
