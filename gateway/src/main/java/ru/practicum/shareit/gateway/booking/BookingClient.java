package ru.practicum.shareit.gateway.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.gateway.booking.dto.BookingInputDto;
import ru.practicum.shareit.gateway.booking.dto.BookingState;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl) {
        super(serverUrl);
    }

    public ResponseEntity<Object> createBooking(Long userId, BookingInputDto bookingInputDto) {
        return post(API_PREFIX, userId, bookingInputDto);
    }

    public ResponseEntity<Object> approveBooking(Long userId, Long bookingId, Boolean approved) {
        String url = API_PREFIX + "/" + bookingId + "?approved=" + approved;
        return patch(url, userId, null);
    }

    public ResponseEntity<Object> getBooking(Long userId, Long bookingId) {
        return get(API_PREFIX + "/" + bookingId, userId, null);
    }

    public ResponseEntity<Object> getUserBookings(Long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        return get(API_PREFIX, userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(Long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        return get(API_PREFIX + "/owner", userId, parameters);
    }
}
