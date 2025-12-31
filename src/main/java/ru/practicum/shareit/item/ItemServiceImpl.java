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
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
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

    @Transactional
    @Override
    public ItemResponseDto createItem(Long ownerId, ItemRequestDto itemRequestDto) {
        log.debug("Creating item for ownerId: {}, item name: {}", ownerId, itemRequestDto.getName());

        User owner = userService.getUserEntity(ownerId);

        validateItemRequestDto(itemRequestDto);

        Item item = ItemMapper.toItem(itemRequestDto, owner);
        Item savedItem = itemRepository.save(item);

        log.info("Item created with id: {}", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Transactional
    @Override
    public ItemResponseDto updateItem(Long ownerId, Long itemId, ItemRequestDto itemRequestDto) {
        log.debug("Updating item with id: {} for ownerId: {}", itemId, ownerId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь", itemId));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("обновить", "вещь", itemId);
        }

        updateItemFields(item, itemRequestDto);
        Item savedItem = itemRepository.save(item);

        log.info("Item with id: {} updated", itemId);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemResponseDto getItem(Long itemId, Long userId) {
        log.debug("Getting item with id: {} for user: {}", itemId, userId);

        if (itemId == null) {
            throw new ValidationException("itemId", "не может быть null");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь", itemId));

        Booking lastBooking = null;
        Booking nextBooking = null;

        if (userId != null && item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            lastBooking = bookingRepository.findLastBookingForItem(itemId, now).orElse(null);
            nextBooking = bookingRepository.findNextBookingForItem(itemId, now).orElse(null);
        }

        List<Comment> comments = commentRepository.findByItemId(itemId);

        return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemResponseDto> getItemsOfOwner(Long ownerId) {
        log.debug("Getting items for ownerId: {}", ownerId);

        if (ownerId == null) {
            throw new ValidationException("ownerId", "не может быть null");
        }

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();

        List<Booking> lastBookings = bookingRepository.findLastBookingsForItems(itemIds, now);
        List<Booking> nextBookings = bookingRepository.findNextBookingsForItems(itemIds, now);

        Map<Long, Booking> lastBookingByItemId = lastBookings.stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking
                ));

        Map<Long, Booking> nextBookingByItemId = nextBookings.stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking
                ));

        List<Comment> allComments = commentRepository.findByItemIdIn(itemIds);
        Map<Long, List<Comment>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId()
                ));
        
        return items.stream()
                .map(item -> {
                    Booking lastBooking = lastBookingByItemId.get(item.getId());
                    Booking nextBooking = nextBookingByItemId.get(item.getId());
                    List<Comment> comments = commentsByItemId.getOrDefault(
                            item.getId(), Collections.emptyList()
                    );

                    return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
                })
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

        if (commentDto.getText() == null || commentDto.getText().trim().isEmpty()) {
            throw new ValidationException("text", "Текст комментария не может быть пустым");
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
    public List<ItemResponseDto> search(String text) {
        log.debug("Searching items with text: {}", text);

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchText = text.trim().toLowerCase();
        return itemRepository.searchAvailableItems(searchText).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void validateItemRequestDto(ItemRequestDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException("name", "Название не может быть пустым");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new ValidationException("description", "Описание не может быть пустым");
        }
        if (dto.getAvailable() == null) {
            throw new ValidationException("available", "Статус доступности не может быть null");
        }
    }

    private void updateItemFields(Item item, ItemRequestDto dto) {
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            item.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null && !dto.getDescription().trim().isEmpty()) {
            item.setDescription(dto.getDescription().trim());
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
    }
}