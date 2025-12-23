package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long ownerId, ItemDto dto) {
        log.debug("Creating item for ownerId: {}, item name: {}", ownerId, dto.getName());

        User owner = userService.getUserEntity(ownerId);

        validateItemDtoForCreate(dto);

        if (dto.getAvailable() == null) {
            dto.setAvailable(false);
        }

        Item item = ItemMapper.toItem(dto, owner);
        Item savedItem = itemRepository.save(item);

        log.info("Item created with id: {}", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    private void validateItemDtoForCreate(ItemDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException("name", "Название не может быть пустым");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new ValidationException("description", "Описание не может быть пустым");
        }
        if (dto.getAvailable() == null) {
            throw new ValidationException("available", "Поле available обязательно");
        }
    }

    @Override
    public ItemDto getItem(Long itemId, Long userId) {  // Добавьте параметр userId
        log.debug("Getting item with id: {} for user: {}", itemId, userId);

        if (itemId == null) {
            throw new ValidationException("itemId", "не может быть null");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь", itemId));

        Booking lastBooking = null;
        Booking nextBooking = null;

        // Показываем бронирования только владельцу вещи
        if (userId != null && item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            lastBooking = bookingRepository.findLastBookingForItem(itemId, now).orElse(null);
            nextBooking = bookingRepository.findNextBookingForItem(itemId, now).orElse(null);
        }

        List<Comment> comments = commentRepository.findByItemId(itemId);

        return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemDto> getItemsOfOwner(Long ownerId) {
        log.debug("Getting items for ownerId: {}", ownerId);

        if (ownerId == null) {
            throw new ValidationException("ownerId", "не может быть null");
        }

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        Map<Long, Booking> lastBookings = new HashMap<>();
        Map<Long, Booking> nextBookings = new HashMap<>();
        Map<Long, List<Comment>> commentsByItem = new HashMap<>();

        for (Long itemId : itemIds) {
            lastBookings.put(itemId,
                    bookingRepository.findLastBookingForItem(itemId, LocalDateTime.now()).orElse(null));
            nextBookings.put(itemId,
                    bookingRepository.findNextBookingForItem(itemId, LocalDateTime.now()).orElse(null));
        }

        List<Comment> allComments = commentRepository.findByItemIdIn(itemIds);
        for (Comment comment : allComments) {
            commentsByItem.computeIfAbsent(comment.getItem().getId(), k -> new ArrayList<>())
                    .add(comment);
        }

        return items.stream()
                .map(item -> ItemMapper.toItemDto(
                        item,
                        lastBookings.get(item.getId()),
                        nextBookings.get(item.getId()),
                        commentsByItem.getOrDefault(item.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        log.debug("Adding comment for item: {} by user: {}", itemId, userId);

        User author = userService.getUserEntity(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь", itemId));

        boolean hasBooked = bookingRepository.hasUserBookedItem(itemId, userId, LocalDateTime.now());
        if (!hasBooked) {
            throw new ValidationException("Комментарий", "можно оставлять только после бронирования");
        }

        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        log.info("Comment added for item: {} by user: {}", itemId, userId);
        return CommentMapper.toCommentDto(savedComment);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto dto) {
        log.debug("Updating item with id: {} for ownerId: {}", itemId, ownerId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь", itemId));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("обновить", "вещь", itemId);
        }

        Item updatedItem = ItemMapper.toItem(dto, item);
        Item savedItem = itemRepository.save(updatedItem);

        log.info("Item with id: {} updated", itemId);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto getItem(Long itemId) {
        // Для обратной совместимости
        return getItem(itemId, null);
    }

    @Override
    public List<ItemDto> search(String text) {
        log.debug("Searching items with text: {}", text);

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchText = text.trim();
        return itemRepository.searchAvailableItems(searchText).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}