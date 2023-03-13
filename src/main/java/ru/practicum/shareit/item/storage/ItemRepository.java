package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository {
    Item addItem(Item item, User user);

    Optional<Item> getItemById(Long itemId);

    List<Item> getAllUsersItems(Long userId);

    Item updateItem(Item item, Long userId);

    List<Item> searchAvailableItems(String text);

    UserDto getOwner(Long itemId);
}
