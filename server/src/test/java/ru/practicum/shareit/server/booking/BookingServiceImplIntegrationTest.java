package ru.practicum.shareit.server.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.server.booking.dto.BookingInputDto;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.booking.dto.BookingState;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.exception.ForbiddenException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.UserServiceImpl;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({BookingServiceImpl.class, UserServiceImpl.class})
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(new User(null, "Owner", "owner@mail.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@mail.com"));

        item = itemRepository.save(new Item(
                null,
                "Drill",
                "Power drill",
                true,
                owner,
                null,
                null,
                null
        ));
    }

    @Test
    void createBooking_ShouldCreateWaitingBooking() {
        BookingInputDto dto = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item.getId()
        );

        BookingResponseDto result =
                bookingService.createBooking(booker.getId(), dto);

        assertNotNull(result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(item.getId(), result.getItem().getId());
    }

    @Test
    void createBooking_WhenOwnerBooksOwnItem_ShouldThrowNotFound() {
        BookingInputDto dto = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item.getId()
        );

        assertThrows(NotFoundException.class, () ->
                bookingService.createBooking(owner.getId(), dto)
        );
    }

    @Test
    void approveBooking_ShouldApproveBooking() {
        BookingResponseDto created =
                bookingService.createBooking(booker.getId(),
                        new BookingInputDto(
                                LocalDateTime.now().plusDays(1),
                                LocalDateTime.now().plusDays(2),
                                item.getId()
                        ));

        BookingResponseDto approved =
                bookingService.approveBooking(owner.getId(), created.getId(), true);

        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void approveBooking_WhenNotOwner_ShouldThrowForbidden() {
        BookingResponseDto created =
                bookingService.createBooking(booker.getId(),
                        new BookingInputDto(
                                LocalDateTime.now().plusDays(1),
                                LocalDateTime.now().plusDays(2),
                                item.getId()
                        ));

        assertThrows(ForbiddenException.class, () ->
                bookingService.approveBooking(booker.getId(), created.getId(), true)
        );
    }

    @Test
    void getBooking_ShouldReturnBookingForOwnerAndBooker() {
        BookingResponseDto created =
                bookingService.createBooking(booker.getId(),
                        new BookingInputDto(
                                LocalDateTime.now().plusDays(1),
                                LocalDateTime.now().plusDays(2),
                                item.getId()
                        ));

        BookingResponseDto byBooker =
                bookingService.getBooking(booker.getId(), created.getId());
        BookingResponseDto byOwner =
                bookingService.getBooking(owner.getId(), created.getId());

        assertEquals(created.getId(), byBooker.getId());
        assertEquals(created.getId(), byOwner.getId());
    }

    @Test
    void getBooking_WhenOtherUser_ShouldThrowForbidden() {
        User stranger = userRepository.save(
                new User(null, "Stranger", "s@mail.com"));

        BookingResponseDto created =
                bookingService.createBooking(booker.getId(),
                        new BookingInputDto(
                                LocalDateTime.now().plusDays(1),
                                LocalDateTime.now().plusDays(2),
                                item.getId()
                        ));

        assertThrows(ForbiddenException.class, () ->
                bookingService.getBooking(stranger.getId(), created.getId())
        );
    }

    @Test
    void getUserBookings_All_ShouldReturnBookings() {
        bookingService.createBooking(booker.getId(),
                new BookingInputDto(
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2),
                        item.getId()
                ));

        List<BookingResponseDto> result =
                bookingService.getUserBookings(
                        booker.getId(), BookingState.ALL, 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_Waiting_ShouldReturnWaitingBookings() {
        bookingService.createBooking(booker.getId(),
                new BookingInputDto(
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2),
                        item.getId()
                ));

        List<BookingResponseDto> result =
                bookingService.getOwnerBookings(
                        owner.getId(), BookingState.WAITING, 0, 10);

        assertEquals(1, result.size());
        assertEquals(BookingStatus.WAITING, result.get(0).getStatus());
    }
}
