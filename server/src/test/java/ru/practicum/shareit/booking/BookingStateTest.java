package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.server.ShareItServerApplication;
import ru.practicum.shareit.server.booking.dto.BookingState;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = ShareItServerApplication.class)
class BookingStateTest {

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void shouldHaveAllStates(BookingState state) {
        assertThat(state).isNotNull();
    }

    @Test
    void shouldHaveCorrectStateNames() {
        assertThat(BookingState.ALL.name()).isEqualTo("ALL");
        assertThat(BookingState.CURRENT.name()).isEqualTo("CURRENT");
        assertThat(BookingState.PAST.name()).isEqualTo("PAST");
        assertThat(BookingState.FUTURE.name()).isEqualTo("FUTURE");
        assertThat(BookingState.WAITING.name()).isEqualTo("WAITING");
        assertThat(BookingState.REJECTED.name()).isEqualTo("REJECTED");
    }

    @Test
    void shouldParseFromString() {
        assertThat(BookingState.valueOf("ALL")).isEqualTo(BookingState.ALL);
        assertThat(BookingState.valueOf("CURRENT")).isEqualTo(BookingState.CURRENT);
        assertThat(BookingState.valueOf("PAST")).isEqualTo(BookingState.PAST);
    }
}