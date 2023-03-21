package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.util.Variables;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PatchMapping("/{bookingId}")
    public BookingDto updateBooking(@PathVariable Long bookingId,
                                    @NotEmpty @RequestHeader(value = Variables.HEADER_USER_ID) Long userId,
                                    @NotEmpty @RequestParam Boolean approved) {
        return bookingService.approve(bookingId, userId, approved);
    }

    @PostMapping
    public BookingDto addBooking(@Valid @RequestBody BookingDtoShort bookingDtoShort,
                                 @NotEmpty @RequestHeader(value = Variables.HEADER_USER_ID) long userId) {
        return bookingService.addBooking(bookingDtoShort, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable Long bookingId,
                                 @NotEmpty @RequestHeader(value = Variables.HEADER_USER_ID) Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllBookingsByUser(@RequestParam(defaultValue = "ALL") String state,
                                                 @NotEmpty @RequestHeader(value = Variables.HEADER_USER_ID) Long userId) {
        return bookingService.getAllBookingsByUser(state, userId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsByOwner(@RequestParam(defaultValue = "ALL") String state,
                                                  @NotEmpty @RequestHeader(value = Variables.HEADER_USER_ID) Long userId) {
        return bookingService.gettAllBookingsByOwner(state, userId);
    }
}
