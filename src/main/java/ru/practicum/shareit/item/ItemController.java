package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.Variables;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto addItem(@Valid @RequestBody ItemDto itemDto,
                           @RequestHeader(value = Variables.HEADER_USER_ID) Long userId) {
        return itemService.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto,
                              @RequestHeader(value = Variables.HEADER_USER_ID) Long userId,
                              @PathVariable long itemId) {
        return itemService.updateItem(itemDto, itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllUsersItems(@NotEmpty @RequestHeader(value = Variables.HEADER_USER_ID) Long userId) {
        return itemService.getAllUsersItems(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId,
                               @NotEmpty @RequestHeader(value = Variables.HEADER_USER_ID) Long userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchAvailableItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId,
                                 @NotEmpty @RequestHeader(value = Variables.HEADER_USER_ID) Long userId,
                                 @Valid @RequestBody CommentDto commentDto) {
        return itemService.addComment(itemId, userId, commentDto);
    }
}
