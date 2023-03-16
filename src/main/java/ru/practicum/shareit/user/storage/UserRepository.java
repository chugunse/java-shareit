package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface UserRepository {

    User addUser(User user);

    Optional<User> getUserById(Long userId);

    Collection<User> getAllUsers();

    User updateUser(User user);

    void deleteUser(Long userId);
}
