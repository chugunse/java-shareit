package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.model.NotFoundException;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.RequestsRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.RequestMapper;
import ru.practicum.shareit.util.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestsRepository requestsRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto addRequest(long userId, ItemRequestDto itemRequestDto) {
        User user = UserMapper.toUser(userService.getUserById(userId));
        ItemRequest itemRequest = ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .requester(user)
                .build();
        return RequestMapper.toDto(requestsRepository.save(itemRequest));
    }

    @Override
    public ItemRequestDto getById(long userId, long requestId) {
        UserDto userDto = userService.getUserById(userId);
        ItemRequest itemRequest = requestsRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("request с id = %d не найден", requestId)));
        itemRequest.setItems(itemRepository.findAllByItemRequest(itemRequest));
        ItemRequestDto itemRequestDto = RequestMapper.toDto(itemRequest);
        itemRequestDto.setRequester(userDto);
        return itemRequestDto;
    }

    @Override
    public List<ItemRequestDto> getAllUserRequest(long userId) {
        userService.getUserById(userId);
        return requestsRepository.findAllByRequesterIdOrderByCreatedDesc(userId).stream()
                .peek(itemRequest -> itemRequest.setItems(itemRepository.findAllByItemRequest(itemRequest)))
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequest(long userId, int from, int size) {
        UserMapper.toUser(userService.getUserById(userId));
        Pageable page = PageRequest.of(from / size, size, Sort.by("created"));
        return requestsRepository.findAllByRequesterIdIsNot(userId, page).stream()
                .peek(itemRequest -> itemRequest.setItems(itemRepository.findAllByItemRequest(itemRequest)))
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());

    }
}
