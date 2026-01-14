package ru.practicum.shareit.server.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.server.booking.dto.BookingInputDto;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(BookingServiceImpl.class)
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TestEntityManager em;

    @MockBean
    private UserService userService;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = em.persistFlushFind(
                new User(null, "Owner", "owner@mail.com")
        );

        booker = em.persistFlushFind(
                new User(null, "Booker", "booker@mail.com")
        );

        item = em.persistFlushFind(
                new Item(null, "Drill", "Power drill", true, owner, null, null, null)
        );

        when(userService.getUserEntity(owner.getId())).thenReturn(owner);
        when(userService.getUserEntity(booker.getId())).thenReturn(booker);
    }

    @Test
    void createBooking_shouldCreateWaitingBooking() {
        BookingInputDto dto = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item.getId()
        );

        BookingResponseDto result =
                bookingService.createBooking(booker.getId(), dto);

        assertNotNull(result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }
}