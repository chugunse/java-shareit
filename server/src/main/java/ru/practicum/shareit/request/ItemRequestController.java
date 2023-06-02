package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;

import static ru.practicum.shareit.util.Variables.HEADER_USER_ID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final RequestService requestService;

    @PostMapping
    public ItemRequestDto addRequest(@RequestHeader(value = HEADER_USER_ID) Long userId,
                                     @RequestBody ItemRequestDto itemRequestDto) {
        return requestService.addRequest(userId, itemRequestDto);
    }

    @GetMapping("{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(value = HEADER_USER_ID) Long userId,
                                         @PathVariable Long requestId) {
        return requestService.getById(userId, requestId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllUserRequest(
            @RequestHeader(value = HEADER_USER_ID) Long userId) {
        return requestService.getAllUserRequest(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(
            @RequestHeader(value = HEADER_USER_ID) Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return requestService.getAllRequest(userId, from, size);
    }
}
