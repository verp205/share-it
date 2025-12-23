package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.validation.ValidationGroups;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    @Null(groups = ValidationGroups.OnCreate.class, message = "ID должен быть null при создании")
    private Long id;

    @NotBlank(groups = ValidationGroups.OnCreate.class, message = "Название не может быть пустым")
    private String name;

    @NotBlank(groups = ValidationGroups.OnCreate.class, message = "Описание не может быть пустым")
    private String description;

    @NotNull(groups = ValidationGroups.OnCreate.class, message = "Статус доступности не может быть null")
    private Boolean available;

    private Long requestId;

    private BookingShortDto lastBooking;

    private BookingShortDto nextBooking;

    private List<CommentDto> comments;
}