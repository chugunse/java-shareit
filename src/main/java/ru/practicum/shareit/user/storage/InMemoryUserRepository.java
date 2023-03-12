package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private long id = 0;
    public Map<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        user.setId(++id);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public User updateUser(User user) {
        if (user.getName() != null) {
            users.get(user.getId()).setName(user.getName());
        }
        if (user.getEmail() != null) {
            users.get(user.getId()).setEmail(user.getEmail());
        }
        return users.get(user.getId());
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }
}
