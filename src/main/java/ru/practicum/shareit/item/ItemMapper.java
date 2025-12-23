package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto toItemDto(Item item, Booking lastBooking, Booking nextBooking, List<Comment> comments) {
        if (item == null) {
            return null;
        }
        Long ownerId = item.getOwner() != null ? item.getOwner().getId() : null;
        BookingShortDto lastBookingDto = BookingMapper.toBookingShortDto(lastBooking);
        BookingShortDto nextBookingDto = BookingMapper.toBookingShortDto(nextBooking);

        List<CommentDto> commentDtos = comments != null
                ? comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList())
                : Collections.emptyList();

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                ownerId,
                lastBookingDto,
                nextBookingDto,
                commentDtos
        );
    }

    public static Item toItem(ItemDto dto, User owner) {
        if (dto == null) {
            return null;
        }
        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable() != null ? dto.getAvailable() : false);
        item.setOwner(owner);
        return item;
    }

    public static ItemDto toItemDto(Item item) {
        return toItemDto(item, null, null, Collections.emptyList());
    }

    public static Item toItem(ItemDto dto, Item existingItem) {
        if (dto == null || existingItem == null) {
            return null;
        }
        if (dto.getName() != null) {
            existingItem.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            existingItem.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            existingItem.setAvailable(dto.getAvailable());
        }
        return existingItem;
    }
}