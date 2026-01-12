package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.ShareItServerApplication;
import ru.practicum.shareit.server.booking.BookingService;
import ru.practicum.shareit.server.booking.dto.BookingInputDto;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.comment.dto.CommentDto;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.ItemService;
import ru.practicum.shareit.server.item.dto.ItemRequestDto;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ContextConfiguration(classes = ShareItServerApplication.class)
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        // Создаем владельца
        UserDto ownerDto = new UserDto(null, "Owner", "owner@email.com");
        UserDto owner = userService.createUser(ownerDto);
        ownerId = owner.getId();

        // Создаем бронировщика
        UserDto bookerDto = new UserDto(null, "Booker", "booker@email.com");
        UserDto booker = userService.createUser(bookerDto);
        bookerId = booker.getId();

        // Создаем вещь
        ItemRequestDto itemDto = new ItemRequestDto(null, "Дрель",
                "Аккумуляторная дрель", true, null);
        ItemResponseDto item = itemService.createItem(ownerId, itemDto);
        itemId = item.getId();
    }

    @Test
    void createItem_withValidData_shouldCreateItem() {
        // Given
        ItemRequestDto newItemDto = new ItemRequestDto(null, "Молоток",
                "Строительный молоток", true, null);

        // When
        ItemResponseDto createdItem = itemService.createItem(ownerId, newItemDto);

        // Then
        assertThat(createdItem).isNotNull();
        assertThat(createdItem.getId()).isNotNull();
        assertThat(createdItem.getName()).isEqualTo("Молоток");
        assertThat(createdItem.getAvailable()).isTrue();
        assertThat(createdItem.getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void createItem_withInvalidData_shouldThrowException() {
        // Given - пустое имя
        ItemRequestDto invalidItemDto = new ItemRequestDto(null, "",
                "Описание", true, null);

        // When & Then
        assertThrows(ValidationException.class,
                () -> itemService.createItem(ownerId, invalidItemDto));
    }

    @Test
    void getItemsOfOwner_whenOwnerHasItems_shouldReturnItemsWithBookingsAndComments() {
        // When
        List<ItemResponseDto> items = itemService.getItemsOfOwner(ownerId);

        // Then
        assertThat(items).hasSize(1);
        ItemResponseDto item = items.get(0);
        assertThat(item.getId()).isEqualTo(itemId);
        assertThat(item.getName()).isEqualTo("Дрель");
        assertThat(item.getLastBooking()).isNull(); // Нет прошлых бронирований
        assertThat(item.getNextBooking()).isNull(); // Нет будущих бронирований
        assertThat(item.getComments()).isEmpty(); // Нет комментариев
    }

    @Test
    void searchItems_whenTextMatches_shouldReturnAvailableItems() {
        // When
        List<ItemResponseDto> results = itemService.search("дрель");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Дрель");
        assertThat(results.get(0).getAvailable()).isTrue();
    }

    @Test
    void searchItems_whenItemNotAvailable_shouldNotReturnItem() {
        // Given - делаем вещь недоступной
        ItemRequestDto updateDto = new ItemRequestDto(itemId, "Дрель",
                "Аккумуляторная дрель", false, null);
        itemService.updateItem(ownerId, itemId, updateDto);

        // When
        List<ItemResponseDto> results = itemService.search("дрель");

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void addComment_whenUserBookedItem_shouldAddComment() throws InterruptedException {
        // Given - создаем бронирование
        BookingInputDto bookingDto = new BookingInputDto(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                itemId
        );

        // Ждем немного чтобы дата начала была в будущем
        Thread.sleep(100);

        BookingResponseDto booking =
                bookingService.createBooking(bookerId, bookingDto);

        bookingService.approveBooking(ownerId, booking.getId(), true);

        // Ждем окончания бронирования
        Thread.sleep(1100); // Ждем 1.1 секунды чтобы бронирование закончилось

        // When - добавляем комментарий
        CommentDto commentDto = new CommentDto(null, "Отличная дрель!", null, null);
        CommentDto addedComment = itemService.addComment(bookerId, itemId, commentDto);

        // Then
        assertThat(addedComment).isNotNull();
        assertThat(addedComment.getText()).isEqualTo("Отличная дрель!");
        assertThat(addedComment.getAuthorName()).isEqualTo("Booker");

        // Проверяем, что комментарий добавился к вещи
        ItemResponseDto item = itemService.getItem(itemId, ownerId);
        assertThat(item.getComments()).hasSize(1);
        assertThat(item.getComments().get(0).getText()).isEqualTo("Отличная дрель!");
    }

    @Test
    void addComment_whenUserNotBookedItem_shouldThrowException() {
        // Given - другой пользователь
        UserDto anotherUserDto = new UserDto(null, "Another", "another@email.com");
        UserDto anotherUser = userService.createUser(anotherUserDto);

        CommentDto commentDto = new CommentDto(null, "Хорошая вещь!", null, null);

        // When & Then
        assertThrows(ValidationException.class,
                () -> itemService.addComment(anotherUser.getId(), itemId, commentDto));
    }

    @Test
    void getItem_whenUserIsOwner_shouldReturnWithBookings() {
        // Given - создаем бронирование
        BookingInputDto bookingDto = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );
        BookingResponseDto booking =
                bookingService.createBooking(bookerId, bookingDto);

        bookingService.approveBooking(ownerId, booking.getId(), true);


        // When
        ItemResponseDto item = itemService.getItem(itemId, ownerId);

        // Then
        assertThat(item).isNotNull();
        assertThat(item.getNextBooking()).isNotNull(); // Должно быть будущее бронирование
        assertThat(item.getNextBooking().getBookerId()).isEqualTo(bookerId);
    }

    @Test
    void getItem_whenUserIsNotOwner_shouldReturnWithoutBookings() {
        // Given - другой пользователь
        UserDto anotherUserDto = new UserDto(null, "Another", "another@email.com");
        UserDto anotherUser = userService.createUser(anotherUserDto);

        // When
        ItemResponseDto item = itemService.getItem(itemId, anotherUser.getId());

        // Then
        assertThat(item).isNotNull();
        assertThat(item.getLastBooking()).isNull(); // Не владелец - не видит бронирования
        assertThat(item.getNextBooking()).isNull();
    }
}