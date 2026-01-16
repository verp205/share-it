package ru.practicum.shareit.server.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.comment.dto.CommentDto;
import ru.practicum.shareit.server.item.dto.ItemRequestDto;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDto createItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                      @RequestBody ItemRequestDto itemRequestDto) {
        return itemService.createItem(ownerId, itemRequestDto);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto updateItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                      @PathVariable Long itemId,
                                      @RequestBody ItemRequestDto itemRequestDto) {
        return itemService.updateItem(ownerId, itemId, itemRequestDto);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getItem(@PathVariable Long itemId,
                                   @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        return itemService.getItem(itemId, userId);
    }

    @GetMapping
    public List<ItemResponseDto> getItemsOfOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getItemsOfOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> searchItems(@RequestParam String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long itemId,
                                 @RequestBody CommentDto commentDto) {
        return itemService.addComment(userId, itemId, commentDto);
    }
}