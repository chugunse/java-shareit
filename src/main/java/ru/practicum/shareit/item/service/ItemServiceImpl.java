package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.model.AccessException;
import ru.practicum.shareit.exceptions.model.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.service.ItemMapper.toItem;
import static ru.practicum.shareit.item.service.ItemMapper.toItemDto;
import static ru.practicum.shareit.user.service.UserMapper.toUser;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto addItem(ItemDto itemDto, Long userId) {
        userService.getUserById(userId);
        return toItemDto(itemRepository.addItem(toItem(itemDto), toUser(userService.getUserById(userId))));
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        return toItemDto(itemRepository.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("item по id %d не найден", itemId))));
    }

    @Override
    public List<ItemDto> getAllUsersItems(Long userId) {
        return itemRepository.getAllUsersItems(userId).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long itemId, Long userId) {
        getItemById(itemId);
        userService.getUserById(userId);
        itemDto.setId(itemId);
        if (!itemRepository.getOwner(itemId).getId().equals(userService.getUserById(userId).getId())) {
            throw new AccessException(String
                    .format("пользователь с id = %d не имеет доступа правки item с id = %d", userId, itemId));
        }
        return toItemDto(itemRepository.updateItem(toItem(itemDto), userId));
    }

    @Override
    public List<ItemDto> searchAvailableItems(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.searchAvailableItems(text).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }
}
