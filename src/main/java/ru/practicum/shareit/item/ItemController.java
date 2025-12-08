package ru.practicum.shareit.item;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.ValidationGroups;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Validated
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(value = "X-Sharer-User-Id", required = true)
                          @Positive(message = "ID владельца должно быть положительным числом") Long ownerId,
                          @Validated(ValidationGroups.OnCreate.class) @RequestBody ItemDto dto) {
        return itemService.createItem(ownerId, dto);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(value = "X-Sharer-User-Id", required = true)
                          @Positive(message = "ID владельца должно быть положительным числом") Long ownerId,
                          @PathVariable @Positive(message = "ID вещи должно быть положительным числом") Long id,
                          @Validated(ValidationGroups.OnUpdate.class) @RequestBody ItemDto dto) {
        return itemService.updateItem(ownerId, id, dto);
    }

    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable @Positive(message = "ID вещи должно быть положительным числом") Long id) {
        return itemService.getItem(id);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader(value = "X-Sharer-User-Id", required = true)
                                  @Positive(message = "ID владельца должно быть положительным числом") Long ownerId) {
        return itemService.getItemsOfOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(defaultValue = "") String text) {
        return itemService.search(text);
    }
}