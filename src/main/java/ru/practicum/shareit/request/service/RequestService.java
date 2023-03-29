package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface RequestService {
    ItemRequestDto addRequest(long userId, ItemRequestDto itemRequestDto);

    ItemRequestDto getById(long userId, long requestId);

    List<ItemRequestDto> getAllUserRequest(long userId);

    List<ItemRequestDto> getAllRequest(long userId, int from, int size);

}
