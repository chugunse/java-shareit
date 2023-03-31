package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.util.Variables;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PatchMapping("/{bookingId}")
    public BookingDto updateBooking(@PathVariable Long bookingId,
                                    @RequestHeader(value = Variables.HEADER_USER_ID) Long userId,
                                    @RequestParam Boolean approved) {
        return bookingService.approve(bookingId, userId, approved);
    }

    @PostMapping
    public BookingDto addBooking(@Valid @RequestBody BookingDtoShort bookingDtoShort,
                                 @RequestHeader(value = Variables.HEADER_USER_ID) Long userId) {
        return bookingService.addBooking(bookingDtoShort, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable Long bookingId,
                                 @RequestHeader(value = Variables.HEADER_USER_ID) Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllBookingsByUser(@RequestParam(defaultValue = "ALL") String state,
                                                 @RequestHeader(value = Variables.HEADER_USER_ID) Long userId,
                                                 @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                 @Positive @RequestParam(defaultValue = "10") Integer size) {
        return bookingService.getAllBookingsByUser(state, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsByOwner(@RequestParam(defaultValue = "ALL") String state,
                                                  @RequestHeader(value = Variables.HEADER_USER_ID) Long userId,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                  @Positive @RequestParam(defaultValue = "10") Integer size) {
        return bookingService.gettAllBookingsByOwner(state, userId, from, size);
    }
}
