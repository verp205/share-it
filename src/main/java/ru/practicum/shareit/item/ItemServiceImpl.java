package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private final Map<Long, Item> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final UserService userService;

    public ItemServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItemDto createItem(Long ownerId, ItemDto dto) {
        log.debug("Creating item for ownerId: {}, item name: {}", ownerId, dto.getName());

        if (ownerId == null) {
            throw new ValidationException("ownerId", "не может быть null");
        }

        User owner = userService.getUserEntity(ownerId);

        Item item = ItemMapper.toItem(dto, owner, null);
        item.setId(idGenerator.getAndIncrement());
        storage.put(item.getId(), item);

        log.info("Item created with id: {}", item.getId());
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto dto) {
        log.debug("Updating item with id: {} for ownerId: {}", itemId, ownerId);

        if (ownerId == null) {
            throw new ValidationException("ownerId", "не может быть null");
        }

        if (itemId == null) {
            throw new ValidationException("itemId", "не может быть null");
        }

        Item item = storage.get(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь", itemId);
        }

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("обновить", "вещь", itemId);
        }

        if (dto.getName() != null) {
            item.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }

        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }

        storage.put(itemId, item);

        log.info("Item with id: {} updated", itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItem(Long itemId) {
        log.debug("Getting item with id: {}", itemId);

        if (itemId == null) {
            throw new ValidationException("itemId", "не может быть null");
        }

        Item item = storage.get(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь", itemId);
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsOfOwner(Long ownerId) {
        log.debug("Getting items for ownerId: {}", ownerId);

        if (ownerId == null) {
            throw new ValidationException("ownerId", "не может быть null");
        }

        return storage.values().stream()
                .filter(i -> i.getOwner().getId().equals(ownerId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        log.debug("Searching items with text: {}", text);

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase().trim();

        return storage.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(i -> (i.getName() != null && i.getName().toLowerCase().contains(searchText)) ||
                        (i.getDescription() != null && i.getDescription().toLowerCase().contains(searchText)))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}