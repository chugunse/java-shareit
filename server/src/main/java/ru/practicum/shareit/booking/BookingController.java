package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.util.Variables.HEADER_USER_ID;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PatchMapping("/{bookingId}")
    public BookingDto updateBooking(@PathVariable Long bookingId,
                                    @RequestHeader(value = HEADER_USER_ID) Long userId,
                                    @RequestParam Boolean approved) {
        return bookingService.approve(bookingId, userId, approved);
    }

    @PostMapping
    public BookingDto addBooking(@RequestBody BookingDtoShort bookingDtoShort,
                                 @RequestHeader(value = HEADER_USER_ID) Long userId) {
        return bookingService.addBooking(bookingDtoShort, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable Long bookingId,
                                 @RequestHeader(value = HEADER_USER_ID) Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllBookingsByUser(@RequestParam(defaultValue = "ALL") String state,
                                                 @RequestHeader(value = HEADER_USER_ID) Long userId,
                                                 @RequestParam(defaultValue = "0") Integer from,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        return bookingService.getAllBookingsByUser(state, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsByOwner(@RequestParam(defaultValue = "ALL") String state,
                                                  @RequestHeader(value = HEADER_USER_ID) Long userId,
                                                  @RequestParam(defaultValue = "0") Integer from,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        return bookingService.gettAllBookingsByOwner(state, userId, from, size);
    }
}
