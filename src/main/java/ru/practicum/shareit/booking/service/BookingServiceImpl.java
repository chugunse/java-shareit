package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.exception.BookingUnavailableException;
import ru.practicum.shareit.booking.exception.DoubleApproveException;
import ru.practicum.shareit.booking.exception.TimeDataException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.model.BadRequestException;
import ru.practicum.shareit.exceptions.model.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public BookingDto addBooking(BookingDtoShort bookingDtoShort, long bookerId) {
        log.info(String.format("BookingService addBooking от юзера %d", bookerId));
        if (bookingDtoShort.getEnd().isBefore(bookingDtoShort.getStart()) ||
                bookingDtoShort.getEnd().equals(bookingDtoShort.getStart())) {
            log.info("таймдатаекс");
            throw new TimeDataException(String
                    .format("недопустимое время бронирования start = %s  end = %s",
                            bookingDtoShort.getStart(), bookingDtoShort.getEnd()));
        }
        User booker = UserMapper.toUser(userService.getUserById(bookerId));
        Item item = ItemMapper.toItem(itemService.getItemById(bookingDtoShort.getItemId(), bookerId));
        if (itemService.getOwnerId(item.getId()) == bookerId) {
            throw new NotFoundException("владелец не может сам у себя арендовать )))");
        }
        if (item.getAvailable()) {
            Booking booking = Booking.builder()
                    .start(bookingDtoShort.getStart())
                    .end(bookingDtoShort.getEnd())
                    .item(item)
                    .booker(booker)
                    .status(BookingStatus.WAITING)
                    .build();
            return BookingMapper.toBookingDto(bookingRepository.save(booking));
        } else {
            log.info("BookingUnavailableException");
            throw new BookingUnavailableException(String.format("%s с id = %d не доступна для бронирования",
                    item.getName(), item.getId()));
        }
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking по id = %d не найден", bookingId)));
        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwnerId().equals(userId)) {
            return BookingMapper.toBookingDto(booking);
        } else {
            throw new NotFoundException(String
                    .format("пользователь с id=%d не имеет доступа к брони с id=%d", userId, bookingId));
        }
    }

    @Override
    public List<BookingDto> getAllBookingsByUser(String state, Long userId) {
        userService.getUserById(userId);
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case "ALL":
                return BookingMapper.listToBookingDto(bookingRepository.findAllByBooker_IdOrderByStartDesc(userId));
            case "CURRENT":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllByBooker_IdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(userId, now, now));
            case "PAST":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(userId, now));
            case "FUTURE":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllByBooker_IdAndStartIsAfterOrderByStartDesc(userId, now));
            case "WAITING":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllByBooker_IdAndStartIsAfterAndStatusIsOrderByStartDesc(userId, now,
                                BookingStatus.WAITING));
            case "REJECTED":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllByBooker_IdAndStatusIsOrderByStartDesc(userId, BookingStatus.REJECTED));

        }
        throw new BadRequestException(String.format("Unknown state: %s", state));
    }

    @Override
    public List<BookingDto> gettAllBookingsByOwner(String state, Long ownerId) {
        userService.getUserById(ownerId);
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case "ALL":
                return BookingMapper.listToBookingDto(bookingRepository.findAllBookingsOwner(ownerId));
            case "CURRENT":
                return BookingMapper.listToBookingDto(bookingRepository.findAllCurrentBookingsOwner(ownerId, now));
            case "PAST":
                return BookingMapper.listToBookingDto(bookingRepository.findAllPastBookingsOwner(ownerId, now));
            case "FUTURE":
                return BookingMapper.listToBookingDto(bookingRepository.findAllFutureBookingsOwner(ownerId, now));
            case "WAITING":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllWaitingBookingsOwner(ownerId, now, BookingStatus.WAITING));
            case "REJECTED":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllRejectedBookingsOwner(ownerId, BookingStatus.REJECTED));
        }
        throw new BadRequestException(String.format("Unknown state: %s", state));
    }

    @Transactional
    @Override
    public BookingDto approve(long bookingId, long userId, Boolean approve) {
        BookingDto booking = getBookingById(bookingId, userId);
        log.info(String.format("patch1 bookingId=%d, userId=%d, approve=%s", bookingId, userId, approve));

        if (itemService.getOwnerId(booking.getItem().getId()).equals(userId)
                && booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new DoubleApproveException("Статус бронирования вещи ранее уже был подтвержден");
        }
        if (!itemService.getOwnerId(booking.getItem().getId()).equals(userId)) {
            throw new NotFoundException("подтвержать и отклонять статус бронирования может только владелец вещи");
        }
        if (approve) {
            booking.setStatus(BookingStatus.APPROVED);
            log.info(String.format("patchDo bookingId=%d, userId=%d, approve=%s", bookingId, userId, approve));
            bookingRepository.update(BookingStatus.APPROVED, bookingId);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
            log.info(String.format("patchDo bookingId=%d, userId=%d, approve=%s", bookingId, userId, approve));
            bookingRepository.update(BookingStatus.REJECTED, bookingId);
        }
        return booking;
    }


}
