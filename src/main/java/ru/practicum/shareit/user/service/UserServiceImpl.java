package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.model.DuplicateException;
import ru.practicum.shareit.exceptions.model.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.service.UserMapper.toUser;
import static ru.practicum.shareit.user.service.UserMapper.toUserDto;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto addUser(UserDto userDto) {
        checkDuplicatEmail(userDto);
        return toUserDto(userRepository.addUser(toUser(userDto)));
    }

    @Override
    public UserDto getUserById(Long userId) {
        return toUserDto(userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("user по id %d не найден", userId))));
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        return userRepository.getAllUsers().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        getUserById(userId);
        userDto.setId(userId);
        checkDuplicatEmail(userDto);
        userDto.setId(userId);
        return toUserDto(userRepository.updateUser(toUser(userDto)));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteUser(userId);
    }

    private void checkDuplicatEmail(UserDto userTest) {
        if (userTest.getEmail() != null) {
            if (getAllUsers().stream().filter(user -> !user.getId().equals(userTest.getId()))
                    .anyMatch(user -> user.getEmail().equals(userTest.getEmail()))) {
                throw new DuplicateException("введенный email уже используется");
            }
        }
    }
}
