package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Service
public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long userId);

    ItemDto getItemById(Long itemId, Long userId);

    List<ItemDto> getAllUsersItems(Long userId);

    ItemDto updateItem(ItemDto itemDto, Long itemId, Long userId);

    List<ItemDto> searchAvailableItems(String text);

    Long getOwnerId(Long itemId);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);
}
