package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(Long ownerId, ItemDto dto);

    ItemDto updateItem(Long ownerId, Long itemId, ItemDto dto);

    ItemDto getItem(Long itemId);

    List<ItemDto> getItemsOfOwner(Long ownerId);

    List<ItemDto> search(String text);
}

