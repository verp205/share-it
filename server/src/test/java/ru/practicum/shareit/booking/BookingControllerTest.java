package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.ShareItServerApplication;
import ru.practicum.shareit.server.booking.BookingController;
import ru.practicum.shareit.server.booking.BookingService;
import ru.practicum.shareit.server.booking.dto.BookingInputDto;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.booking.dto.BookingState;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;
import ru.practicum.shareit.server.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.server.booking.model.BookingStatus.APPROVED;
import static ru.practicum.shareit.server.booking.model.BookingStatus.WAITING;

@WebMvcTest(controllers = BookingController.class)
@ContextConfiguration(classes = ShareItServerApplication.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        // Given
        Long userId = 1L;
        BookingInputDto bookingDto = new BookingInputDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L
        );

        UserDto booker = new UserDto(userId, "Booker", "booker@example.com");
        ItemResponseDto item = new ItemResponseDto(1L, "Дрель", "Описание",
                true, 2L, null, null, null, List.of());
        BookingResponseDto createdBooking = new BookingResponseDto(
                1L,
                bookingDto.getStart(),
                bookingDto.getEnd(),
                WAITING,
                booker,
                item
        );

        when(bookingService.createBooking(eq(userId), any(BookingInputDto.class)))
                .thenReturn(createdBooking);

        // When & Then
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(userId))
                .andExpect(jsonPath("$.item.id").value(1));
    }

    @Test
    void approveBooking_shouldReturnUpdatedBooking() throws Exception {
        // Given
        Long userId = 1L;
        Long bookingId = 1L;
        boolean approved = true;

        UserDto booker = new UserDto(2L, "Booker", "booker@example.com");
        ItemResponseDto item = new ItemResponseDto(1L, "Дрель", "Описание",
                true, userId, null, null, null, List.of());
        BookingResponseDto updatedBooking = new BookingResponseDto(
                bookingId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                APPROVED,
                booker,
                item
        );

        when(bookingService.approveBooking(userId, bookingId, approved))
                .thenReturn(updatedBooking);

        // When & Then
        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBooking_shouldReturnBooking() throws Exception {
        // Given
        Long userId = 1L;
        Long bookingId = 1L;

        UserDto booker = new UserDto(userId, "Booker", "booker@example.com");
        ItemResponseDto item = new ItemResponseDto(1L, "Дрель", "Описание",
                true, 2L, null, null, null, List.of());
        BookingResponseDto booking = new BookingResponseDto(
                bookingId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                WAITING,
                booker,
                item
        );

        when(bookingService.getBooking(userId, bookingId)).thenReturn(booking);

        // When & Then
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(userId));
    }

    @Test
    void getUserBookings_shouldReturnBookingsList() throws Exception {
        // Given
        Long userId = 1L;
        BookingState state = BookingState.ALL;
        Integer from = 0;
        Integer size = 10;

        UserDto booker = new UserDto(userId, "Booker", "booker@example.com");
        ItemResponseDto item = new ItemResponseDto(1L, "Дрель", "Описание",
                true, 2L, null, null, null, List.of());
        BookingResponseDto booking = new BookingResponseDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                WAITING,
                booker,
                item
        );

        when(bookingService.getUserBookings(userId, state, from, size))
                .thenReturn(List.of(booking));

        // When & Then
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void getOwnerBookings_shouldReturnBookingsList() throws Exception {
        // Given
        Long userId = 1L;
        BookingState state = BookingState.ALL;
        Integer from = 0;
        Integer size = 10;

        UserDto booker = new UserDto(2L, "Booker", "booker@example.com");
        ItemResponseDto item = new ItemResponseDto(1L, "Дрель", "Описание",
                true, userId, null, null, null, List.of());
        BookingResponseDto booking = new BookingResponseDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                WAITING,
                booker,
                item
        );

        when(bookingService.getOwnerBookings(userId, state, from, size))
                .thenReturn(List.of(booking));

        // When & Then
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].item.ownerId").value(userId));
    }
}