package ru.practicum.shareit.item;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(Long ownerId, ItemDto dto);

    ItemDto updateItem(Long ownerId, Long itemId, ItemDto dto);

    ItemDto getItem(Long itemId);

    ItemDto getItem(Long itemId, Long userId);

    List<ItemDto> getItemsOfOwner(Long ownerId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}