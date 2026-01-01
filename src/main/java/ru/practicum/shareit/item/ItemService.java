package ru.practicum.shareit.item;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {

    ItemResponseDto createItem(Long ownerId, ItemRequestDto dto);

    @Transactional
    ItemResponseDto updateItem(Long ownerId, Long itemId, ItemRequestDto itemRequestDto);

    ItemResponseDto getItem(Long itemId, Long userId);

    List<ItemResponseDto> getItemsOfOwner(Long ownerId);

    List<ItemResponseDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}