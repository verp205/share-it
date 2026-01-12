package ru.practicum.shareit.server.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingIT {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking pastBooking;
    private Booking currentBooking;
    private Booking futureBooking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@email.com");
        entityManager.persist(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@email.com");
        entityManager.persist(booker);

        item = new Item();
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);
        entityManager.persist(item);

        // Прошлое бронирование
        pastBooking = new Booking();
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        pastBooking.setCreated(LocalDateTime.now().minusDays(2));
        entityManager.persist(pastBooking);

        // Текущее бронирование
        currentBooking = new Booking();
        currentBooking.setStart(LocalDateTime.now().minusHours(1));
        currentBooking.setEnd(LocalDateTime.now().plusHours(1));
        currentBooking.setItem(item);
        currentBooking.setBooker(booker);
        currentBooking.setStatus(BookingStatus.APPROVED);
        currentBooking.setCreated(LocalDateTime.now().minusHours(2));
        entityManager.persist(currentBooking);

        // Будущее бронирование
        futureBooking = new Booking();
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));
        futureBooking.setItem(item);
        futureBooking.setBooker(booker);
        futureBooking.setStatus(BookingStatus.WAITING);
        futureBooking.setCreated(LocalDateTime.now());
        entityManager.persist(futureBooking);

        entityManager.flush();
    }

    @Test
    void findByBookerIdOrderByStartDesc_shouldReturnBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(booker.getId(), pageable);

        assertThat(bookings).hasSize(3);
        assertThat(bookings.get(0).getId()).isEqualTo(futureBooking.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(currentBooking.getId());
        assertThat(bookings.get(2).getId()).isEqualTo(pastBooking.getId());
    }

    @Test
    void findPastBookingsByBooker_shouldReturnOnlyPastBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findPastBookingsByBooker(
                booker.getId(), LocalDateTime.now(), pageable);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(pastBooking.getId());
    }

    @Test
    void findCurrentBookingsByBooker_shouldReturnOnlyCurrentBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findCurrentBookingsByBooker(
                booker.getId(), LocalDateTime.now(), pageable);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(currentBooking.getId());
    }

    @Test
    void findByItemOwnerIdOrderByStartDesc_shouldReturnOwnerBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(owner.getId(), pageable);

        assertThat(bookings).hasSize(3);
    }

    @Test
    void existsOverlappingBookings_shouldReturnTrueWhenOverlapExists() {
        boolean exists = bookingRepository.existsOverlappingBookings(
                item.getId(),
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(2)
        );

        assertThat(exists).isTrue();
    }

    @Test
    void findLastBookingForItem_shouldReturnPastApprovedBooking() {
        LocalDateTime now = LocalDateTime.now();
        var lastBooking = bookingRepository.findLastBookingForItem(item.getId(), now);

        assertThat(lastBooking).isPresent();
        assertThat(lastBooking.get().getId()).isEqualTo(pastBooking.getId());
    }
}