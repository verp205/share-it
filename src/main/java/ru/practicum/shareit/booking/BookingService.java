package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(Long userId, BookingInputDto bookingInputDto);

    BookingResponseDto approveBooking(Long ownerId, Long bookingId, boolean approved);

    BookingResponseDto getBooking(Long userId, Long bookingId);

    List<BookingResponseDto> getUserBookings(Long userId, BookingState state, int from, int size);

    List<BookingResponseDto> getOwnerBookings(Long ownerId, BookingState state, int from, int size);
}