package ru.practicum.shareit.server.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.comment.CommentMapper;
import ru.practicum.shareit.server.comment.dto.CommentDto;
import ru.practicum.shareit.server.comment.model.Comment;
import ru.practicum.shareit.server.comment.CommentRepository;
import ru.practicum.shareit.server.exception.ForbiddenException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.dto.ItemRequestDto;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.ItemRequestRepository;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;

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
    private final ItemRequestRepository itemRequestRepository;

    @Transactional
    @Override
    public ItemResponseDto createItem(Long ownerId, ItemRequestDto itemRequestDto) {
        log.debug("Creating item for ownerId: {}, item name: {}", ownerId, itemRequestDto.getName());

        User owner = userService.getUserEntity(ownerId);
        validateItemRequestDto(itemRequestDto, true); // проверка только при создании

        Item item = ItemMapper.toItem(itemRequestDto, owner);
        log.debug("Item mapped: {}", item);

        if (itemRequestDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemRequestDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос", itemRequestDto.getRequestId()));
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        log.info("Item created with id: {}", savedItem.getId());

        return ItemMapper.toItemDto(savedItem);
    }

    @Transactional
    @Override
    public ItemResponseDto updateItem(Long ownerId, Long itemId, ItemRequestDto itemRequestDto) {
        log.debug("Updating item with id: {} for ownerId: {}", itemId, ownerId);

        Item item = itemRepository.findByIdWithOwnerAndRequest(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь", itemId));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("обновить", "вещь", itemId);
        }

        updateItemFields(item, itemRequestDto);

        if (itemRequestDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemRequestDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос", itemRequestDto.getRequestId()));
            item.setRequest(request);
        }

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

        Item item = itemRepository.findByIdWithOwnerAndRequest(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь", itemId));

        Booking lastBooking = null;
        Booking nextBooking = null;

        if (userId != null && item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            lastBooking = bookingRepository.findLastBookingForItem(itemId, now).orElse(null);
            nextBooking = bookingRepository.findNextBookingForItem(itemId, now).orElse(null);
        }

        List<Comment> comments = commentRepository.findByItemId(itemId);
        if (comments == null) comments = Collections.emptyList();

        return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemResponseDto> getItemsOfOwner(Long ownerId) {
        log.debug("Getting items for ownerId: {}", ownerId);

        if (ownerId == null) {
            throw new ValidationException("ownerId", "не может быть null");
        }

        List<Item> items = itemRepository.findByOwnerIdWithOwnerAndRequest(ownerId);
        if (items.isEmpty()) return Collections.emptyList();

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();

        List<Booking> lastBookings = Optional.ofNullable(bookingRepository.findLastBookingsForItems(itemIds, now))
                .orElse(Collections.emptyList());
        List<Booking> nextBookings = Optional.ofNullable(bookingRepository.findNextBookingsForItems(itemIds, now))
                .orElse(Collections.emptyList());
        List<Comment> allComments = Optional.ofNullable(commentRepository.findByItemIdIn(itemIds))
                .orElse(Collections.emptyList());

        Map<Long, Booking> lastBookingByItemId = lastBookings.stream()
                .collect(Collectors.toMap(b -> b.getItem().getId(), b -> b));
        Map<Long, Booking> nextBookingByItemId = nextBookings.stream()
                .collect(Collectors.toMap(b -> b.getItem().getId(), b -> b));
        Map<Long, List<Comment>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        return items.stream()
                .map(item -> ItemMapper.toItemDto(
                        item,
                        lastBookingByItemId.get(item.getId()),
                        nextBookingByItemId.get(item.getId()),
                        commentsByItemId.getOrDefault(item.getId(), Collections.emptyList())
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

        Boolean hasBooked = Boolean.TRUE.equals(bookingRepository.hasUserBookedItem(itemId, userId, LocalDateTime.now()));
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

        if (text == null || text.isBlank()) return Collections.emptyList();

        String searchText = text.trim().toLowerCase();
        return itemRepository.searchAvailableItems(searchText).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void validateItemRequestDto(ItemRequestDto dto, boolean isCreate) {
        if (isCreate) {
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
