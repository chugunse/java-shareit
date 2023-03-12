package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

@Service
public interface UserService {
    UserDto addUser(UserDto userDto);

    UserDto getUserById(Long userId);

    Collection<UserDto> getAllUsers();

    UserDto updateUser(UserDto userDto, Long userId);

    void deleteUser(Long userId);
}
