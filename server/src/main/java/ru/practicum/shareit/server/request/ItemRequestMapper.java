package ru.practicum.shareit.server.request;

import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto dto, User requester) {
        if (dto == null) {
            return null;
        }
        return new ItemRequest(
                null,
                dto.getDescription(),
                requester,
                LocalDateTime.now(),
                Collections.emptyList()
        );
    }

    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest request) {
        if (request == null) {
            return null;
        }

        List<ItemDto> items = Collections.emptyList();
        if (request.getItems() != null) {
            items = request.getItems().stream()
                    .map(item -> new ItemDto(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.getAvailable(),
                            request.getId(),
                            item.getOwner() != null ? item.getOwner().getId() : null
                    ))
                    .collect(Collectors.toList());
        }

        return new ItemRequestResponseDto(
                request.getId(),
                request.getDescription(),
                request.getRequester() != null ? request.getRequester().getId() : null,
                request.getCreated(),
                items
        );
    }
}