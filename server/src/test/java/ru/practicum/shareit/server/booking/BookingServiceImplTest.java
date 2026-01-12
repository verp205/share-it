package ru.practicum.shareit.server.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.server.booking.dto.BookingInputDto;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.booking.dto.BookingState;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.exception.ForbiddenException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private BookingInputDto bookingInputDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@email.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@email.com");

        item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);

        bookingInputDto = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L
        );

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(bookingInputDto.getStart());
        booking.setEnd(bookingInputDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking.setCreated(LocalDateTime.now());
    }

    @Test
    void createBooking_shouldCreateBookingSuccessfully() {
        when(userService.getUserEntity(2L)).thenReturn(booker);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingBookings(anyLong(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.createBooking(2L, bookingInputDto);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_whenUserNotFound_shouldThrowNotFoundException() {
        when(userService.getUserEntity(2L)).thenThrow(new NotFoundException("Пользователь", 2L));

        assertThatThrownBy(() -> bookingService.createBooking(2L, bookingInputDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь");
    }

    @Test
    void createBooking_whenItemNotFound_shouldThrowNotFoundException() {
        when(userService.getUserEntity(2L)).thenReturn(booker);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(2L, bookingInputDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь");
    }

    @Test
    void createBooking_whenItemNotAvailable_shouldThrowValidationException() {
        item.setAvailable(false);
        when(userService.getUserEntity(2L)).thenReturn(booker);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(2L, bookingInputDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("недоступна");
    }

    @Test
    void createBooking_whenBookingOwnItem_shouldThrowNotFoundException() {
        when(userService.getUserEntity(1L)).thenReturn(owner);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(1L, bookingInputDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("нельзя забронировать свою же вещь");
    }

    @Test
    void createBooking_whenEndBeforeStart_shouldThrowValidationException() {
        bookingInputDto = new BookingInputDto(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1),
                1L
        );

        when(userService.getUserEntity(2L)).thenReturn(booker);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(2L, bookingInputDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("дата окончания");
    }

    @Test
    void createBooking_whenEndEqualsStart_shouldThrowValidationException() {
        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);
        bookingInputDto = new BookingInputDto(sameTime, sameTime, 1L);

        when(userService.getUserEntity(2L)).thenReturn(booker);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(2L, bookingInputDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("дата окончания");
    }

    @Test
    void createBooking_whenOverlappingBookingExists_shouldThrowValidationException() {
        when(userService.getUserEntity(2L)).thenReturn(booker);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingBookings(anyLong(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(2L, bookingInputDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("уже занято");
    }

    @Test
    void approveBooking_shouldApproveSuccessfully() {
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.approveBooking(1L, 1L, true);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void approveBooking_shouldRejectSuccessfully() {
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.approveBooking(1L, 1L, false);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approveBooking_whenBookingNotFound_shouldThrowNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 1L, true))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Бронирование");
    }

    @Test
    void approveBooking_whenUserNotOwner_shouldThrowForbiddenException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(999L, 1L, true))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("подтвердить бронирование");
    }

    @Test
    void approveBooking_whenStatusNotWaiting_shouldThrowValidationException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 1L, true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("уже обработан");
    }

    @Test
    void getBooking_shouldReturnBookingSuccessfully() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBooking(2L, 1L); // Booker requesting

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBooking_whenBookingNotFound_shouldThrowNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBooking(1L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Бронирование");
    }

    @Test
    void getBooking_whenUserNotAuthorized_shouldThrowForbiddenException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBooking(999L, 1L)) // Random user
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("просмотреть бронирование");
    }

    @Test
    void getUserBookings_withAllState_shouldReturnAllBookings() {
        when(userService.getUserEntity(2L)).thenReturn(booker);
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(2L), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, BookingState.ALL, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getUserBookings_withCurrentState_shouldReturnCurrentBookings() {
        when(userService.getUserEntity(2L)).thenReturn(booker);
        when(bookingRepository.findCurrentBookingsByBooker(eq(2L), any(LocalDateTime.class), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, BookingState.CURRENT, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getUserBookings_withPastState_shouldReturnPastBookings() {
        when(userService.getUserEntity(2L)).thenReturn(booker);
        when(bookingRepository.findPastBookingsByBooker(eq(2L), any(LocalDateTime.class), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, BookingState.PAST, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getUserBookings_withFutureState_shouldReturnFutureBookings() {
        when(userService.getUserEntity(2L)).thenReturn(booker);
        when(bookingRepository.findFutureBookingsByBooker(eq(2L), any(LocalDateTime.class), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, BookingState.FUTURE, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getUserBookings_withWaitingState_shouldReturnWaitingBookings() {
        when(userService.getUserEntity(2L)).thenReturn(booker);
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(2L), eq(BookingStatus.WAITING), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, BookingState.WAITING, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getUserBookings_withRejectedState_shouldReturnRejectedBookings() {
        booking.setStatus(BookingStatus.REJECTED);
        when(userService.getUserEntity(2L)).thenReturn(booker);
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(2L), eq(BookingStatus.REJECTED), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, BookingState.REJECTED, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getOwnerBookings_withAllState_shouldReturnAllOwnerBookings() {
        when(userService.getUserEntity(1L)).thenReturn(owner);
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(1L), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, BookingState.ALL, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getOwnerBookings_withCurrentState_shouldReturnCurrentOwnerBookings() {
        when(userService.getUserEntity(1L)).thenReturn(owner);
        when(bookingRepository.findCurrentBookingsByOwner(eq(1L), any(LocalDateTime.class), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, BookingState.CURRENT, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getOwnerBookings_withPastState_shouldReturnPastOwnerBookings() {
        when(userService.getUserEntity(1L)).thenReturn(owner);
        when(bookingRepository.findPastBookingsByOwner(eq(1L), any(LocalDateTime.class), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, BookingState.PAST, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getOwnerBookings_withFutureState_shouldReturnFutureOwnerBookings() {
        when(userService.getUserEntity(1L)).thenReturn(owner);
        when(bookingRepository.findFutureBookingsByOwner(eq(1L), any(LocalDateTime.class), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, BookingState.FUTURE, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getOwnerBookings_withWaitingState_shouldReturnWaitingOwnerBookings() {
        when(userService.getUserEntity(1L)).thenReturn(owner);
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(eq(1L), eq(BookingStatus.WAITING), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, BookingState.WAITING, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getOwnerBookings_withRejectedState_shouldReturnRejectedOwnerBookings() {
        booking.setStatus(BookingStatus.REJECTED);
        when(userService.getUserEntity(1L)).thenReturn(owner);
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(eq(1L), eq(BookingStatus.REJECTED), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, BookingState.REJECTED, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void getUserBookings_whenUserNotFound_shouldThrowNotFoundException() {
        when(userService.getUserEntity(2L)).thenThrow(new NotFoundException("Пользователь", 2L));

        assertThatThrownBy(() -> bookingService.getUserBookings(2L, BookingState.ALL, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь");
    }

    @Test
    void getOwnerBookings_whenUserNotFound_shouldThrowNotFoundException() {
        when(userService.getUserEntity(1L)).thenThrow(new NotFoundException("Пользователь", 1L));

        assertThatThrownBy(() -> bookingService.getOwnerBookings(1L, BookingState.ALL, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь");
    }
}