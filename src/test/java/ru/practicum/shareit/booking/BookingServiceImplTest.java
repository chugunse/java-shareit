package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.exception.BookingUnavailableException;
import ru.practicum.shareit.booking.exception.DoubleApproveException;
import ru.practicum.shareit.booking.exception.TimeDataException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.model.BadRequestException;
import ru.practicum.shareit.exceptions.model.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @InjectMocks
    BookingServiceImpl bookingService;
    @Mock
    UserService userService;
    @Mock
    ItemService itemService;
    @Mock
    BookingRepository bookingRepository;

    User user = new User(1L, "testUser", "test@email.com");
    UserDto userDto = new UserDto(1L, "testUser", "test@email.com");
    BookingDtoShort bookingDtoShort = BookingDtoShort.builder()
            .start(LocalDateTime.now())
            .end(LocalDateTime.now().plusHours(1L))
            .itemId(1L)
            .build();
    ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("testItem")
            .description("testDescription")
            .available(true)
            .requestId(1L)
            .build();
    Item item = Item.builder()
            .id(1L)
            .name("testItem")
            .description("testDescription")
            .available(true)
            .ownerId(99L)
            .build();
    Booking booking = Booking.builder()
            .booker(user)
            .id(1L)
            .status(BookingStatus.APPROVED)
            .item(item).build();

    @Test
    void createBooking_shouldThrowExceptionWhenUserIsOwner() {
        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(itemDto);
        when(itemService.getOwnerId(anyLong())).thenReturn(1L);
        Exception e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(bookingDtoShort, 1L));
        assertThat(e.getMessage(), equalTo("владелец не может сам у себя арендовать )))"));
    }

    @Test
    void createBookins_shouldThrowTimeDataException() {
        BookingDtoShort bookingBadTime = BookingDtoShort.builder()
                .start(LocalDateTime.now().plusHours(1L))
                .end(LocalDateTime.now().minusHours(1L))
                .itemId(1L)
                .build();
        Exception e = Assertions.assertThrows(TimeDataException.class,
                () -> bookingService.addBooking(bookingBadTime, 1L));
        assertThat(e.getMessage(), equalTo("недопустимое время бронирования"));

        bookingBadTime.setStart(LocalDateTime.now());
        bookingBadTime.setEnd(bookingBadTime.getStart());

        e = Assertions.assertThrows(TimeDataException.class,
                () -> bookingService.addBooking(bookingBadTime, 1L));
        assertThat(e.getMessage(), equalTo("недопустимое время бронирования"));
    }

    @Test
    void createBookinsUnavailableException() {
        itemDto.setAvailable(false);
        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(itemDto);
        when(itemService.getOwnerId(anyLong())).thenReturn(1L);
        Exception e = Assertions.assertThrows(BookingUnavailableException.class,
                () -> bookingService.addBooking(bookingDtoShort, 2L));
        assertThat(e.getMessage(), equalTo("testItem с id = 1 не доступна для бронирования"));
    }

    @Test
    void getBookingByIdNotFoundException() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        Exception e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(1L, 1L));
        assertThat(e.getMessage(), equalTo("Booking по id = 1 не найден"));
    }

    @Test
    void getBookingByIdAccessException() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        Exception e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(9L, 7L));
        assertThat(e.getMessage(), equalTo("пользователь с id=7 не имеет доступа к брони с id=9"));
    }

    @Test
    void getAllBookingsByUserException() {
        when(userService.getUserById(anyLong())).thenReturn(userDto);
        Exception e = Assertions.assertThrows(BadRequestException.class,
                () -> bookingService.getAllBookingsByUser("хрень", 1L, 0, 10));
        assertThat(e.getMessage(), equalTo("Unknown state: хрень"));
    }

    @Test
    void gettAllBookingsByOwnerException() {
        when(userService.getUserById(anyLong())).thenReturn(userDto);
        Exception e = Assertions.assertThrows(BadRequestException.class,
                () -> bookingService.gettAllBookingsByOwner("хрень", 1L, 0, 10));
        assertThat(e.getMessage(), equalTo("Unknown state: хрень"));
    }

    @Test
    void approveDoubleApproveException() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(itemService.getOwnerId(anyLong()))
                .thenReturn(1L);
        Exception e = Assertions.assertThrows(DoubleApproveException.class,
                () -> bookingService.approve(1L, 1L, true));
        assertThat(e.getMessage(), equalTo("Статус бронирования вещи ранее уже был подтвержден"));
    }

    @Test
    void approveNotFoundException() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(itemService.getOwnerId(anyLong()))
                .thenReturn(2L);
        Exception e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.approve(1L, 1L, true));
        assertThat(e.getMessage(),
                equalTo("подтвержать и отклонять статус бронирования может только владелец вещи"));
    }
}
