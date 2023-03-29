package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.model.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTest {

    @InjectMocks
    UserServiceImpl userService;
    @Mock
    UserRepository repository;

    User user = new User(1L, "testUser", "test@email.com");
    UserDto userDto = new UserDto(1L, "testUser", "test@email.com");
    User user2 = new User(2L, "testUser2", "test2@email.com");
    UserDto user2Dto = new UserDto(2L, "testUser2", "test2@email.com");

    @Test
    public void createUserTest() {
        when(repository.save(any()))
                .thenReturn(user);

        assertThat(userService.addUser(userDto), equalTo(userDto));
    }

    @Test
    public void getUserByIdExistTest() {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        assertThat(userDto, equalTo(userService.getUserById(1L)));
    }

    @Test
    public void getUserByIdNotExistTest() {
        when(repository.findById(anyLong()))
                .thenReturn(empty());

        Exception exception = Assertions.assertThrows(NotFoundException.class, () -> userService.getUserById(1L));
        assertThat(exception.getMessage(), equalTo(String.format("user по id %d не найден", 1)));
    }

    @Test
    public void getAllUsersExistTest() {
        when(repository.findAll())
                .thenReturn(List.of(user, user2));

        List<UserDto> users = userService.getAllUsers();
        assertThat(users, equalTo(List.of(userDto, user2Dto)));
    }

    @Test
    public void getAllUsersNotExistTest() {
        when(repository.findAll())
                .thenReturn(new ArrayList<>());

        assertThat(userService.getAllUsers(), equalTo(new ArrayList<>()));
    }

    @Test
    public void editUserFullExistsTest() {
        User updateUser = new User(1L, "testUpdateUser", "testUpdateUser@email.com");
        UserDto updateUserDto = new UserDto(1L, "testUpdateUser", "testUpdateUser@email.com");

        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(repository.save(any()))
                .thenReturn(updateUser);

        assertThat(userService.updateUser(updateUserDto, 1L), equalTo(updateUserDto));
    }

    @Test
    public void editUserEmptyExistsTest() {
        UserDto updateUserDto = new UserDto();

        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(repository.save(any()))
                .thenReturn(user);

        assertThat(userService.updateUser(updateUserDto, 1L), equalTo(userDto));
    }

    @Test
    public void editUserThrowNotFoundExceptionWhenUserNotExists() {
        when(repository.findById(anyLong()))
                .thenReturn(empty());

        Exception exception = Assertions.assertThrows(NotFoundException.class, () -> userService.getUserById(1L));
        assertThat(exception.getMessage(), equalTo(String.format("user по id %d не найден", 1)));
    }

    @Test
    public void deleteUser_shouldDeleteUser() {
        userService.deleteUser(anyLong());
        verify(repository, times(1)).deleteById(anyLong());
    }
}