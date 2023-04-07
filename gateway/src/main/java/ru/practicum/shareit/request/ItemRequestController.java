package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;
    public static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestHeader(value = HEADER_USER_ID) Long userId,
                                             @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Post userId={}, itemRequestDto={}", userId, itemRequestDto);
        return itemRequestClient.addRequest(userId, itemRequestDto);
    }

    @GetMapping("{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(value = HEADER_USER_ID) Long userId,
                                                 @PathVariable Long requestId) {
        log.info("Get /requestId userdId={}, requestId={}", userId, requestId);
        return itemRequestClient.getById(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUserRequest(
            @RequestHeader(value = HEADER_USER_ID) Long userId) {
        log.info("Get allUserRequest userId={}", userId);
        return itemRequestClient.getAllUserRequest(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader(value = HEADER_USER_ID) Long userId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Get /all userId={}, from={}, size={}", userId, from, size);
        return itemRequestClient.getAllRequest(userId, from, size);
    }
}
