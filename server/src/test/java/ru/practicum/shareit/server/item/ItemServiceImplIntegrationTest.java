package ru.practicum.shareit.server.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.server.comment.dto.CommentDto;
import ru.practicum.shareit.server.exception.ForbiddenException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.dto.ItemRequestDto;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.UserServiceImpl;
import ru.practicum.shareit.server.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ItemServiceImpl.class, UserServiceImpl.class})
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(new User(null, "Owner", "owner@mail.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@mail.com"));
    }

    @Test
    void createItem_ShouldSaveItemInDatabase() {
        ItemRequestDto dto = new ItemRequestDto(
                null,
                "Drill",
                "Powerful drill",
                true,
                null
        );

        ItemResponseDto result = itemService.createItem(owner.getId(), dto);

        assertNotNull(result.getId());
        assertEquals("Drill", result.getName());
        assertEquals(owner.getId(), result.getOwnerId());

        assertTrue(itemRepository.existsById(result.getId()));
    }

    @Test
    void updateItem_ShouldUpdateItemFields() {
        ItemResponseDto created = itemService.createItem(
                owner.getId(),
                new ItemRequestDto(null, "Item", "Desc", true, null)
        );

        ItemRequestDto updateDto = new ItemRequestDto();
        updateDto.setName("Updated");
        updateDto.setAvailable(false);

        ItemResponseDto updated = itemService.updateItem(
                owner.getId(),
                created.getId(),
                updateDto
        );

        assertEquals("Updated", updated.getName());
        assertFalse(updated.getAvailable());
    }

    @Test
    void updateItem_WhenNotOwner_ShouldThrowForbiddenException() {
        ItemResponseDto created = itemService.createItem(
                owner.getId(),
                new ItemRequestDto(null, "Item", "Desc", true, null)
        );

        assertThrows(ForbiddenException.class, () ->
                itemService.updateItem(booker.getId(), created.getId(), new ItemRequestDto())
        );
    }

    @Test
    void getItem_ShouldReturnItem() {
        ItemResponseDto created = itemService.createItem(
                owner.getId(),
                new ItemRequestDto(null, "Item", "Desc", true, null)
        );

        ItemResponseDto result = itemService.getItem(created.getId(), owner.getId());

        assertEquals(created.getId(), result.getId());
        assertEquals("Item", result.getName());
    }

    @Test
    void getItem_WhenNotExists_ShouldThrowNotFound() {
        assertThrows(NotFoundException.class, () ->
                itemService.getItem(999L, owner.getId())
        );
    }

    @Test
    void getItemsOfOwner_ShouldReturnOwnerItems() {
        itemService.createItem(owner.getId(),
                new ItemRequestDto(null, "Item1", "Desc1", true, null));
        itemService.createItem(owner.getId(),
                new ItemRequestDto(null, "Item2", "Desc2", true, null));

        List<ItemResponseDto> items = itemService.getItemsOfOwner(owner.getId());

        assertEquals(2, items.size());
    }

    @Test
    void search_ShouldReturnOnlyAvailableItems() {
        itemService.createItem(owner.getId(),
                new ItemRequestDto(null, "Hammer", "Tool", true, null));
        itemService.createItem(owner.getId(),
                new ItemRequestDto(null, "Hammer old", "Tool", false, null));

        List<ItemResponseDto> result = itemService.search("hammer");

        assertEquals(1, result.size());
        assertTrue(result.get(0).getAvailable());
    }

    @Test
    void search_WhenTextBlank_ShouldReturnEmptyList() {
        List<ItemResponseDto> result = itemService.search(" ");

        assertTrue(result.isEmpty());
    }

    @Test
    void addComment_WhenUserHasNoBooking_ShouldThrowValidationException() {
        ItemResponseDto item = itemService.createItem(
                owner.getId(),
                new ItemRequestDto(null, "Item", "Desc", true, null)
        );

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Nice item");

        assertThrows(ValidationException.class, () ->
                itemService.addComment(booker.getId(), item.getId(), commentDto)
        );
    }
}
