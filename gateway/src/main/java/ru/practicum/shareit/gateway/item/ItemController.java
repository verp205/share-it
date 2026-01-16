package ru.practicum.shareit.gateway.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.comment.CommentDto;
import ru.practicum.shareit.gateway.item.dto.ItemRequestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId,
            @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("POST /items - создание вещи пользователем {}", ownerId);
        return itemClient.createItem(ownerId, itemRequestDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId,
            @PathVariable @Positive Long itemId,
            @RequestBody ItemRequestDto itemRequestDto) {
        log.info("PATCH /items/{} - обновление вещи", itemId);
        return itemClient.updateItem(ownerId, itemId, itemRequestDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(
            @PathVariable @Positive Long itemId,
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        log.info("GET /items/{} - получение вещи", itemId);
        return itemClient.getItem(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsOfOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        log.info("GET /items - получение вещей владельца {}", ownerId);
        return itemClient.getItemsOfOwner(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(
            @RequestParam @NotBlank String text) {
        log.info("GET /items/search?text={} - поиск вещей", text);
        return itemClient.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody CommentDto commentDto) {
        log.info("POST /items/{}/comment - добавление комментария", itemId);
        return itemClient.addComment(userId, itemId, commentDto);
    }
}