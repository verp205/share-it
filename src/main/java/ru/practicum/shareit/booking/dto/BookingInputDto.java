package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingInputDto {

    @FutureOrPresent(message = "Дата начала должна быть в настоящем или будущем")
    @NotNull(message = "Дата начала не может быть пустой")
    private LocalDateTime start;

    @Future(message = "Дата окончания должна быть в будущем")
    @NotNull(message = "Дата окончания не может быть пустой")
    private LocalDateTime end;

    @NotNull(message = "ID вещи не может быть пустым")
    private Long itemId;
}