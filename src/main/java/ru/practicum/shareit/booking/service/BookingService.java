package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;

import java.util.List;

@Service
public interface BookingService {
    BookingDto addBooking(BookingDtoShort bookingDtoShort, long userId);

    BookingDto approve(long bookingId, long userId, Boolean approve);

    BookingDto getBookingById(Long bookingId, Long userId);

    List<BookingDto> getAllBookingsByUser(String state, Long userId, int from, int size);

    List<BookingDto> gettAllBookingsByOwner(String state, Long ownerId, int from, int size);
}
