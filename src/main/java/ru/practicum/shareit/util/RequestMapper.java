package ru.practicum.shareit.util;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class RequestMapper {
    public static ItemRequestDto toDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .requester(UserMapper.toUserDto(itemRequest.getRequester()))
                .items(itemRequest.getItems() != null ? itemRequest.getItems()
                        .stream().map(ItemMapper::toItemDto).collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }

    public static ItemRequest fromDto(ItemRequestDto itemRequestDto) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .build();
    }
}
