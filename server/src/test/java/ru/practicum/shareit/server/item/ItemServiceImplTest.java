package ru.practicum.shareit.server.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.comment.CommentRepository;
import ru.practicum.shareit.server.comment.dto.CommentDto;
import ru.practicum.shareit.server.exception.ForbiddenException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.dto.ItemRequestDto;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.ItemRequestRepository;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private ItemRequestDto itemRequestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Alice", "alice@example.com");
        itemRequestDto = new ItemRequestDto(null, "Drill", "Power drill", true, null);
        item = new Item(1L, "Drill", "Power drill", true, owner, null, new ArrayList<>(), new ArrayList<>());
    }

    @Test
    void createItem_WithValidData_ShouldReturnDto() {
        when(userService.getUserEntity(1L)).thenReturn(owner);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponseDto result = itemService.createItem(1L, itemRequestDto);

        assertNotNull(result);
        assertEquals("Drill", result.getName());
        assertEquals(owner.getId(), result.getOwnerId());
    }

    @Test
    void createItem_WithRequestId_ShouldAttachRequest() {
        ItemRequest request = new ItemRequest(10L, "Need drill", owner, LocalDateTime.now(), List.of());
        itemRequestDto.setRequestId(10L);

        when(userService.getUserEntity(1L)).thenReturn(owner);
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponseDto result = itemService.createItem(1L, itemRequestDto);

        assertNotNull(result);
    }

    @Test
    void updateItem_WhenOwnerMatches_ShouldUpdateFields() {
        when(itemRepository.findByIdWithOwnerAndRequest(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        itemRequestDto.setName("New Drill");
        itemRequestDto.setAvailable(false);

        ItemResponseDto result = itemService.updateItem(1L, 1L, itemRequestDto);

        assertEquals("New Drill", result.getName());
        assertFalse(result.getAvailable());
    }

    @Test
    void updateItem_WhenOwnerMismatch_ShouldThrowForbidden() {
        User otherOwner = new User(2L, "Bob", "bob@example.com");
        item.setOwner(otherOwner);

        when(itemRepository.findByIdWithOwnerAndRequest(1L)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class, () -> itemService.updateItem(1L, 1L, itemRequestDto));
    }

    @Test
    void getItem_WhenOwner_ShouldIncludeBookingsAndComments() {
        when(itemRepository.findByIdWithOwnerAndRequest(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(List.of());

        ItemResponseDto result = itemService.getItem(1L, 1L);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
    }

    @Test
    void getItem_WhenNotFound_ShouldThrowNotFound() {
        when(itemRepository.findByIdWithOwnerAndRequest(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItem(1L, 1L));
    }

    @Test
    void addComment_WhenUserBooked_ShouldSaveComment() {
        CommentDto commentDto = new CommentDto(null, "Great!", "Alice", LocalDateTime.now());
        when(userService.getUserEntity(1L)).thenReturn(owner);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.hasUserBookedItem(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(true);
        when(commentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CommentDto result = itemService.addComment(1L, 1L, commentDto);

        assertEquals("Great!", result.getText());
    }

    @Test
    void addComment_WhenUserNotBooked_ShouldThrowValidation() {
        CommentDto commentDto = new CommentDto(null, "Great!", "Alice", LocalDateTime.now());
        when(userService.getUserEntity(1L)).thenReturn(owner);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.hasUserBookedItem(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThrows(ValidationException.class, () -> itemService.addComment(1L, 1L, commentDto));
    }


    @Test
    void search_ShouldReturnItems() {
        when(itemRepository.searchAvailableItems("drill")).thenReturn(List.of(item));

        List<ItemResponseDto> result = itemService.search("drill");

        assertEquals(1, result.size());
        assertEquals(item.getName(), result.get(0).getName());
    }
}
