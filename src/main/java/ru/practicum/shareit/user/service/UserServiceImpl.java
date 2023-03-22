package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.model.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.util.UserMapper.toUser;
import static ru.practicum.shareit.util.UserMapper.toUserDto;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto addUser(UserDto userDto) {
        return toUserDto(userRepository.save(toUser(userDto)));
    }

    @Transactional
    @Override
    public UserDto getUserById(Long userId) {
        return toUserDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("user по id %d не найден", userId))));
    }

    @Transactional
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        User user = toUser(getUserById(userId));
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
