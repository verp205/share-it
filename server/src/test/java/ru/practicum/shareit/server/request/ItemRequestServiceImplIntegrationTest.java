package ru.practicum.shareit.server.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.UserServiceImpl;
import ru.practicum.shareit.server.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ItemRequestServiceImpl.class, UserServiceImpl.class})
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    private User requester;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        userRepository.deleteAll();

        requester = userRepository.save(new User(null, "Requester", "req@mail.com"));
        anotherUser = userRepository.save(new User(null, "Another", "another@mail.com"));
    }

    @Test
    void createRequest_ShouldSaveRequestInDatabase() {
        ItemRequestDto dto = new ItemRequestDto("Need a drill");

        ItemRequestResponseDto result =
                itemRequestService.createRequest(requester.getId(), dto);

        assertNotNull(result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertEquals(requester.getId(), result.getRequesterId());

        assertTrue(requestRepository.existsById(result.getId()));
    }

    @Test
    void getUserRequests_ShouldReturnOnlyUserRequests() {
        itemRequestService.createRequest(
                requester.getId(),
                new ItemRequestDto("Request 1")
        );
        itemRequestService.createRequest(
                requester.getId(),
                new ItemRequestDto("Request 2")
        );

        itemRequestService.createRequest(
                anotherUser.getId(),
                new ItemRequestDto("Other request")
        );

        List<ItemRequestResponseDto> result =
                itemRequestService.getUserRequests(requester.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .allMatch(r -> r.getRequesterId().equals(requester.getId())));
    }

    @Test
    void getAllRequests_ShouldReturnRequestsOfOtherUsers() {
        itemRequestService.createRequest(
                requester.getId(),
                new ItemRequestDto("Requester request")
        );
        itemRequestService.createRequest(
                anotherUser.getId(),
                new ItemRequestDto("Another user request")
        );

        List<ItemRequestResponseDto> result =
                itemRequestService.getAllRequests(requester.getId(), 0, 10);

        assertEquals(1, result.size());
        assertEquals(anotherUser.getId(), result.get(0).getRequesterId());
    }

    @Test
    void getRequestById_ShouldReturnRequest() {
        ItemRequestResponseDto created =
                itemRequestService.createRequest(
                        requester.getId(),
                        new ItemRequestDto("Need ladder")
                );

        ItemRequestResponseDto result =
                itemRequestService.getRequestById(
                        anotherUser.getId(),
                        created.getId()
                );

        assertEquals(created.getId(), result.getId());
        assertEquals("Need ladder", result.getDescription());
    }

    @Test
    void getRequestById_WhenNotExists_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () ->
                itemRequestService.getRequestById(
                        requester.getId(),
                        999L
                )
        );
    }

    @Test
    void getUserRequests_WhenUserNotExists_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () ->
                itemRequestService.getUserRequests(999L)
        );
    }

    @Test
    void getAllRequests_WhenUserNotExists_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () ->
                itemRequestService.getAllRequests(999L, 0, 10)
        );
    }
}
