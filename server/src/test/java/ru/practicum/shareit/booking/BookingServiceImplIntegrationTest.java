package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.server.booking.dto.BookingState;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.exception.NotFoundException;
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
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

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
    void createBooking_withValidData_shouldCreateBooking() {
        // Given
        BookingInputDto bookingDto = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );

        // When
        BookingResponseDto booking = bookingService.createBooking(bookerId, bookingDto);

        // Then
        assertThat(booking).isNotNull();
        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(booking.getBooker().getId()).isEqualTo(bookerId);
        assertThat(booking.getItem().getId()).isEqualTo(itemId);
    }

    @Test
    void createBooking_whenItemNotAvailable_shouldThrowException() {
        // Given - делаем вещь недоступной
        ItemRequestDto updateDto = new ItemRequestDto(itemId, "Дрель",
                "Аккумуляторная дрель", false, null);
        itemService.updateItem(ownerId, itemId, updateDto);

        BookingInputDto bookingDto = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );

        // When & Then
        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookerId, bookingDto));
    }

    @Test
    void createBooking_whenBookerIsOwner_shouldThrowException() {
        // Given
        BookingInputDto bookingDto = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );

        // When & Then
        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(ownerId, bookingDto));
    }

    @Test
    void createBooking_whenDatesInvalid_shouldThrowException() {
        // Given - дата окончания раньше даты начала
        BookingInputDto bookingDto = new BookingInputDto(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1),
                itemId
        );

        // When & Then
        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookerId, bookingDto));
    }

    @Test
    void approveBooking_whenOwnerApproves_shouldChangeStatus() {
        // Given - создаем бронирование
        BookingInputDto bookingDto = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );
        BookingResponseDto booking = bookingService.createBooking(bookerId, bookingDto);
        Long bookingId = booking.getId();

        // When
        BookingResponseDto approvedBooking = bookingService.approveBooking(ownerId, bookingId, true);

        // Then
        assertThat(approvedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveBooking_whenOwnerRejects_shouldChangeStatus() {
        // Given
        BookingInputDto bookingDto = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );
        BookingResponseDto booking = bookingService.createBooking(bookerId, bookingDto);
        Long bookingId = booking.getId();

        // When
        BookingResponseDto rejectedBooking = bookingService.approveBooking(ownerId, bookingId, false);

        // Then
        assertThat(rejectedBooking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void getUserBookings_whenStateAll_shouldReturnAllBookings() {
        // Given - создаем несколько бронирований
        BookingInputDto booking1 = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );
        BookingInputDto booking2 = new BookingInputDto(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4),
                itemId
        );

        bookingService.createBooking(bookerId, booking1);
        bookingService.createBooking(bookerId, booking2);

        // When
        List<BookingResponseDto> bookings = bookingService.getUserBookings(bookerId,
                BookingState.ALL, 0, 10);

        // Then
        assertThat(bookings).hasSize(2);
    }

    @Test
    void getOwnerBookings_whenStateWaiting_shouldReturnWaitingBookings() {
        // Given
        BookingInputDto bookingDto = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );
        bookingService.createBooking(bookerId, bookingDto);

        // When
        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(ownerId,
                BookingState.WAITING, 0, 10);

        // Then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}