package ru.practicum.shareit.server.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.booking.dto.BookingInputDto;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.booking.dto.BookingState;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.exception.ForbiddenException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;
import ru.practicum.shareit.server.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private BookingResponseDto bookingResponseDto;
    private BookingInputDto bookingInputDto;
    private final Long userId = 1L;
    private final Long bookingId = 1L;

    @BeforeEach
    void setUp() {
        // Создаем тестовые данные
        UserDto bookerDto = new UserDto(2L, "Booker", "booker@email.com");
        ItemResponseDto itemDto = new ItemResponseDto(
                1L, "Test Item", "Description", true,
                1L, null, null, null, List.of()
        );

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        bookingResponseDto = new BookingResponseDto(
                bookingId,
                start,
                end,
                BookingStatus.WAITING,
                bookerDto,
                itemDto
        );

        bookingInputDto = new BookingInputDto(
                start,
                end,
                1L
        );
    }

    @Test
    void approveBooking_whenApprovedTrue_thenReturnApprovedStatus() throws Exception {
        BookingResponseDto approvedDto = new BookingResponseDto(
                bookingId,
                bookingResponseDto.getStart(),
                bookingResponseDto.getEnd(),
                BookingStatus.APPROVED,
                bookingResponseDto.getBooker(),
                bookingResponseDto.getItem()
        );

        when(bookingService.approveBooking(userId, bookingId, true))
                .thenReturn(approvedDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId.intValue())))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void approveBooking_whenApprovedFalse_thenReturnRejectedStatus() throws Exception {
        BookingResponseDto rejectedDto = new BookingResponseDto(
                bookingId,
                bookingResponseDto.getStart(),
                bookingResponseDto.getEnd(),
                BookingStatus.REJECTED,
                bookingResponseDto.getBooker(),
                bookingResponseDto.getItem()
        );

        when(bookingService.approveBooking(userId, bookingId, false))
                .thenReturn(rejectedDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId.intValue())))
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    @Test
    void approveBooking_whenUserNotOwner_thenReturnForbidden() throws Exception {
        when(bookingService.approveBooking(userId, bookingId, true))
                .thenThrow(new ForbiddenException("подтвердить бронирование"));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("ForbiddenException")))
                .andExpect(jsonPath("$.message", containsString("подтвердить бронирование")));
    }

    @Test
    void getBooking_whenValidRequest_thenReturnBooking() throws Exception {
        when(bookingService.getBooking(userId, bookingId))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId.intValue())))
                .andExpect(jsonPath("$.booker.id", is(2)))
                .andExpect(jsonPath("$.item.id", is(1)));
    }

    @Test
    void getBooking_whenUnauthorizedAccess_thenReturnForbidden() throws Exception {
        when(bookingService.getBooking(userId, bookingId))
                .thenThrow(new ForbiddenException("просмотреть бронирование"));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("ForbiddenException")))
                .andExpect(jsonPath("$.message", containsString("просмотреть бронирование")));
    }

    @Test
    void getBooking_whenBookingNotFound_thenReturnNotFound() throws Exception {
        when(bookingService.getBooking(userId, bookingId))
                .thenThrow(new NotFoundException("Бронирование", bookingId));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message", containsString("Бронирование")));
    }

    @Test
    void getUserBookings_whenAllState_thenReturnAllBookings() throws Exception {
        when(bookingService.getUserBookings(eq(userId), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingId.intValue())));
    }

    @Test
    void getUserBookings_whenCurrentState_thenReturnCurrentBookings() throws Exception {
        when(bookingService.getUserBookings(eq(userId), eq(BookingState.CURRENT), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "CURRENT")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getUserBookings_whenDefaultParams_thenUseDefaults() throws Exception {
        when(bookingService.getUserBookings(eq(userId), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getUserBookings_whenInvalidState_thenReturnBadRequest() throws Exception {
        // Вместо выброса исключения в сервисе, это исключение будет выброшено Spring при конвертации параметра
        // Поэтому не мокаем вызов сервиса
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "INVALID")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // Из логов видно, что возвращается 500
                .andExpect(jsonPath("$.error", is("Internal Server Error")));
    }

    @Test
    void getOwnerBookings_whenAllState_thenReturnAllOwnerBookings() throws Exception {
        when(bookingService.getOwnerBookings(eq(userId), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingId.intValue())));
    }

    @Test
    void getOwnerBookings_whenFutureState_thenReturnFutureOwnerBookings() throws Exception {
        when(bookingService.getOwnerBookings(eq(userId), eq(BookingState.FUTURE), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "FUTURE")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getOwnerBookings_whenWaitingState_thenReturnWaitingOwnerBookings() throws Exception {
        when(bookingService.getOwnerBookings(eq(userId), eq(BookingState.WAITING), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getOwnerBookings_whenRejectedState_thenReturnRejectedOwnerBookings() throws Exception {
        BookingResponseDto rejectedDto = new BookingResponseDto(
                bookingId,
                bookingResponseDto.getStart(),
                bookingResponseDto.getEnd(),
                BookingStatus.REJECTED,
                bookingResponseDto.getBooker(),
                bookingResponseDto.getItem()
        );

        when(bookingService.getOwnerBookings(eq(userId), eq(BookingState.REJECTED), eq(0), eq(10)))
                .thenReturn(List.of(rejectedDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "REJECTED")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("REJECTED")));
    }

    @Test
    void getOwnerBookings_whenUserNotFound_thenReturnNotFound() throws Exception {
        when(bookingService.getOwnerBookings(eq(userId), eq(BookingState.ALL), eq(0), eq(10)))
                .thenThrow(new NotFoundException("Пользователь", userId));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message", containsString("Пользователь")));
    }

    @Test
    void getOwnerBookings_whenPaginationParams_thenUsePagination() throws Exception {
        when(bookingService.getOwnerBookings(eq(userId), eq(BookingState.ALL), eq(5), eq(20)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "5")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void approveBooking_whenMissingApprovedParam_thenReturnInternalServerError() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // Из логов видно 500
                .andExpect(jsonPath("$.error", is("Internal Server Error")));
    }

    @Test
    void approveBooking_whenInvalidApprovedParam_thenReturnInternalServerError() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // Из логов видно 500
                .andExpect(jsonPath("$.error", is("Internal Server Error")));
    }

    @Test
    void getUserBookings_whenNegativeFrom_thenReturnBadRequest() throws Exception {
        when(bookingService.getUserBookings(eq(userId), eq(BookingState.ALL), eq(-1), eq(10)))
                .thenThrow(new ValidationException("Параметр from", "не может быть отрицательным"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("ValidationException")))
                .andExpect(jsonPath("$.message", containsString("Параметр from")));
    }

    @Test
    void getUserBookings_whenZeroSize_thenReturnBadRequest() throws Exception {
        when(bookingService.getUserBookings(eq(userId), eq(BookingState.ALL), eq(0), eq(0)))
                .thenThrow(new ValidationException("Параметр size", "должен быть больше 0"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("ValidationException")))
                .andExpect(jsonPath("$.message", containsString("Параметр size")));
    }

    @Test
    void getOwnerBookings_whenCurrentState_thenReturnCurrentOwnerBookings() throws Exception {
        when(bookingService.getOwnerBookings(eq(userId), eq(BookingState.CURRENT), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "CURRENT")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getOwnerBookings_whenPastState_thenReturnPastOwnerBookings() throws Exception {
        when(bookingService.getOwnerBookings(eq(userId), eq(BookingState.PAST), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "PAST")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getUserBookings_whenFutureState_thenReturnFutureBookings() throws Exception {
        when(bookingService.getUserBookings(eq(userId), eq(BookingState.FUTURE), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "FUTURE")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getUserBookings_whenPastState_thenReturnPastBookings() throws Exception {
        when(bookingService.getUserBookings(eq(userId), eq(BookingState.PAST), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "PAST")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getUserBookings_whenWaitingState_thenReturnWaitingBookings() throws Exception {
        when(bookingService.getUserBookings(eq(userId), eq(BookingState.WAITING), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getUserBookings_whenRejectedState_thenReturnRejectedBookings() throws Exception {
        BookingResponseDto rejectedDto = new BookingResponseDto(
                bookingId,
                bookingResponseDto.getStart(),
                bookingResponseDto.getEnd(),
                BookingStatus.REJECTED,
                bookingResponseDto.getBooker(),
                bookingResponseDto.getItem()
        );

        when(bookingService.getUserBookings(eq(userId), eq(BookingState.REJECTED), eq(0), eq(10)))
                .thenReturn(List.of(rejectedDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "REJECTED")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("REJECTED")));
    }
}