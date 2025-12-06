package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserService;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final Map<Long, Item> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final UserService userService;

    public ItemServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItemDto createItem(Long ownerId, ItemDto dto) {
        // Проверка владельца
        if (ownerId == null) {
            throw new IllegalArgumentException("Не указан ID пользователя");
        }

        User owner = userService.getUserEntity(ownerId);
        if (owner == null) {
            throw new NoSuchElementException("Пользователь с id " + ownerId + " не найден");
        }

        // Простые проверки полей
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Название вещи не может быть пустым");
        }

        if (dto.getDescription() == null) {
            throw new IllegalArgumentException("Описание вещи не может быть пустым");
        }

        if (dto.getAvailable() == null) {
            throw new IllegalArgumentException("Статус доступности должен быть указан");
        }

        ItemRequest request = null; // Пока нет реализации запросов

        Item item = ItemMapper.toItem(dto, owner, request);
        item.setId(idGenerator.getAndIncrement());
        storage.put(item.getId(), item);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto dto) {
        Item item = storage.get(itemId);
        if (item == null) {
            throw new NoSuchElementException("Вещь с id " + itemId + " не найдена");
        }

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NoSuchElementException("Пользователь не является владельцем вещи");
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            item.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }

        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItem(Long itemId) {
        Item item = storage.get(itemId);
        if (item == null) {
            throw new NoSuchElementException("Вещь с id " + itemId + " не найдена");
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsOfOwner(Long ownerId) {
        return storage.values().stream()
                .filter(i -> i.getOwner().getId().equals(ownerId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase();

        return storage.values().stream()
                .filter(Item::getAvailable)
                .filter(i -> (i.getName() != null && i.getName().toLowerCase().contains(searchText)) ||
                        (i.getDescription() != null && i.getDescription().toLowerCase().contains(searchText)))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}