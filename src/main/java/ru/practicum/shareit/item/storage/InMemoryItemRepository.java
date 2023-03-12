package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private long id = 0;
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item addItem(Item item, User user) {
        item.setId(++id);
        item.setOwner(user);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> getItemById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> getAllUsersItems(Long userId) {
        return items.values().stream().filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Item updateItem(Item item, Long userId) {
        if (item.getName() != null) {
            items.get(item.getId()).setName(item.getName());
        }
        if (item.getDescription() != null) {
            items.get(item.getId()).setDescription(item.getDescription());

        }
        if (item.getAvailable() != null) {
            items.get(item.getId()).setAvailable(item.getAvailable());
        }
        return items.get(item.getId());
    }

    @Override
    public List<Item> searchAvailableItems(String text) {
        return items.values().stream().filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getOwner(Long itemId) {
        return UserMapper.toUserDto(items.get(itemId).getOwner());
    }
}
