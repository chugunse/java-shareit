package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.model.NotFoundException;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.request.storage.RequestsRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplUnitTest {
    @InjectMocks
    RequestServiceImpl requestService;
    @Mock
    RequestsRepository requestsRepository;
    @Mock
    UserService userService;

    @Mock
    ItemRepository itemRepository;
    UserDto userDto = new UserDto(1L, "testUser", "test@email.com");
    User user = new User(1L, "testUser", "test@email.com");
    ItemRequest itemRequest = ItemRequest.builder()
            .id((1L))
            .description("testDescription")
            .requester(user)
            .build();
    ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(1L)
            .description("testDescription")
            .requester(userDto)
            .items(new ArrayList<>())
            .build();

    @Test
    void createRequestTest() {
        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);
        when(requestsRepository.save(any()))
                .thenReturn(itemRequest);
        assertThat(requestService.addRequest(1L, itemRequestDto), equalTo(itemRequestDto));
    }

    @Test
    void createRequest_UserNotFound() {
        when(userService.getUserById(anyLong()))
                .thenThrow(new NotFoundException("user по id 1 не найден"));

        Exception e = Assertions.assertThrows(NotFoundException.class,
                () -> requestService.addRequest(1L, itemRequestDto));
        assertThat(e.getMessage(), equalTo("user по id 1 не найден"));
    }

    @Test
    void getByIdTest() {
        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);
        when(requestsRepository.findById(any()))
                .thenReturn(Optional.ofNullable(itemRequest));
        when(itemRepository.findAllByItemRequest(any()))
                .thenReturn(new ArrayList<>());
        assertThat(requestService.getById(1L, 1), equalTo(itemRequestDto));
    }

    @Test
    void getByIdUserNotFoundTest() {
        when(userService.getUserById(anyLong()))
                .thenThrow(new NotFoundException("user по id 1 не найден"));

        Exception e = Assertions.assertThrows(NotFoundException.class,
                () -> requestService.getById(1L, 1));
        assertThat(e.getMessage(), equalTo("user по id 1 не найден"));
    }

    @Test
    void getByIdRequestNotFoundTest() {
        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);
        when(requestsRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        Exception e = Assertions.assertThrows(NotFoundException.class,
                () -> requestService.getById(1L, 1));
        assertThat(e.getMessage(), equalTo("request с id = 1 не найден"));
    }

    @Test
    void getByIdItemRequestNotFoundTest() {
        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);
        when(requestsRepository.findById(any()))
                .thenReturn(Optional.empty());
        Exception e = Assertions.assertThrows(NotFoundException.class,
                () -> requestService.getById(1L, 1L));
        assertThat(e.getMessage(), equalTo("request с id = 1 не найден"));
    }

    @Test
    void getAllUserRequestTest() {
        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);
        when(requestsRepository.findAllByRequesterIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByItemRequest(any()))
                .thenReturn(new ArrayList<>());
        assertThat(requestService.getAllUserRequest(1L), equalTo(List.of(itemRequestDto)));
    }

    @Test
    void getAllUserRequest_UserNotFoundTest() {
        when(userService.getUserById(anyLong()))
                .thenThrow(new NotFoundException("user по id 1 не найден"));

        Exception e = Assertions.assertThrows(NotFoundException.class,
                () -> requestService.getAllUserRequest(1L));
        assertThat(e.getMessage(), equalTo("user по id 1 не найден"));
    }

    @Test
    void getAllRequestTest() {
        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);
        when(itemRepository.findAllByItemRequest(any()))
                .thenReturn(new ArrayList<>());
        when(requestsRepository.findAllByRequesterIdIsNot(anyLong(), any()))
                .thenReturn(List.of(itemRequest));
        assertThat(requestService.getAllRequest(1L, 1, 1), equalTo(List.of(itemRequestDto)));
    }

    @Test
    void getAllRequests_UserNotFoundTest() {
        when(userService.getUserById(anyLong()))
                .thenThrow(new NotFoundException("user по id 1 не найден"));

        Exception e = Assertions.assertThrows(NotFoundException.class,
                () -> requestService.getAllRequest(1L, 1, 1));
        assertThat(e.getMessage(), equalTo("user по id 1 не найден"));
    }
}
