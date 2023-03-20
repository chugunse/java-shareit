package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ForItemBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.service.ItemMapper;
import ru.practicum.shareit.user.service.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {
    public static Booking toBooking(BookingDto dto) {
        return Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(ItemMapper.toItem(dto.getItem()))
                .booker(UserMapper.toUser(dto.getBooker()))
                .status(dto.getStatus())
                .build();
    }

    public static BookingDto toBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setBooker(UserMapper.toUserDto(booking.getBooker()));
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setItem(ItemMapper.toItemDto(booking.getItem()));
        dto.setStatus(booking.getStatus());
        return dto;
    }

    public static List<BookingDto> listToBookingDto(List<Booking> bookings) {
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    public static ForItemBookingDto toItemBookingDto(Booking booking) {
        return new ForItemBookingDto(booking.getId(), booking.getBooker().getId(),
                booking.getStart(), booking.getEnd());
    }
}
