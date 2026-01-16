package ru.practicum.shareit.gateway.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.gateway.booking.dto.BookingInputDto;
import ru.practicum.shareit.gateway.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingControllerTest {

    private BookingClient bookingClient;
    private BookingController bookingController;

    @BeforeEach
    void setup() {
        bookingClient = mock(BookingClient.class);
        bookingController = new BookingController(bookingClient);
    }

    @Test
    void createBooking_callsClientAndReturnsResponse() {
        BookingInputDto dto = new BookingInputDto(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                1L
        );
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(dto, HttpStatus.OK);

        when(bookingClient.createBooking(1L, dto)).thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingController.createBooking(1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(bookingClient, times(1)).createBooking(1L, dto);
    }

    @Test
    void approveBooking_callsClientAndReturnsResponse() {
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        when(bookingClient.approveBooking(1L, 1L, true)).thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingController.approveBooking(1L, 1L, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookingClient, times(1)).approveBooking(1L, 1L, true);
    }

    @Test
    void getBooking_callsClientAndReturnsResponse() {
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        when(bookingClient.getBooking(1L, 1L)).thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingController.getBooking(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookingClient, times(1)).getBooking(1L, 1L);
    }

    @Test
    void getUserBookings_callsClientAndReturnsResponse() {
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        when(bookingClient.getUserBookings(1L, BookingState.ALL, 0, 10)).thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingController.getUserBookings(1L, BookingState.ALL, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookingClient, times(1)).getUserBookings(1L, BookingState.ALL, 0, 10);
    }

    @Test
    void getOwnerBookings_callsClientAndReturnsResponse() {
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        when(bookingClient.getOwnerBookings(1L, BookingState.ALL, 0, 10)).thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingController.getOwnerBookings(1L, BookingState.ALL, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookingClient, times(1)).getOwnerBookings(1L, BookingState.ALL, 0, 10);
    }
}
