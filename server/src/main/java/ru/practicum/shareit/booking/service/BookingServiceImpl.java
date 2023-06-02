package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.BookingMapper;
import ru.practicum.shareit.util.ItemMapper;
import ru.practicum.shareit.util.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Transactional
    @Override
    public BookingDto addBooking(BookingDtoShort bookingDtoShort, long bookerId) {
        if (bookingDtoShort.getEnd().isBefore(bookingDtoShort.getStart()) ||
                bookingDtoShort.getEnd().equals(bookingDtoShort.getStart())) {
            throw new TimeDataException("недопустимое время бронирования");
        }
        User booker = UserMapper.toUser(userService.getUserById(bookerId));
        Item item = ItemMapper.toItem(itemService.getItemById(bookingDtoShort.getItemId(), bookerId));
        if (itemService.getOwnerId(item.getId()) == bookerId) {
            throw new NotFoundException("владелец не может сам у себя арендовать )))");
        }
        item.setOwnerId(itemService.getOwnerId(item.getId()));
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
            throw new BookingUnavailableException(String.format("%s с id = %d не доступна для бронирования",
                    item.getName(), item.getId()));
        }
    }

    @Transactional
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


    @Transactional
    @Override
    public List<BookingDto> getAllBookingsByUser(String state, Long userId, int from, int size) {
        userService.getUserById(userId);
        Sort sortByCreated = Sort.by(Sort.Direction.DESC, "start");
        Pageable page = PageRequest.of(from / size, size, sortByCreated);
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case "ALL":
                return BookingMapper.listToBookingDto(bookingRepository.findAllByBooker_Id(userId, page));
            case "CURRENT":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllByBooker_IdAndEndIsAfterAndStartIsBefore(userId, now, now, page));
            case "PAST":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllByBooker_IdAndEndIsBefore(userId, now, page));
            case "FUTURE":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllByBooker_IdAndStartIsAfter(userId, now, page));
            case "WAITING":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllByBooker_IdAndStartIsAfterAndStatusIs(userId, now,
                                BookingStatus.WAITING, page));
            case "REJECTED":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllByBooker_IdAndStatusIs(userId, BookingStatus.REJECTED, page));

        }
        throw new BadRequestException(String.format("Unknown state: %s", state));
    }

    @Transactional
    @Override
    public List<BookingDto> gettAllBookingsByOwner(String state, Long ownerId, int from, int size) {
        userService.getUserById(ownerId);
        Pageable page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case "ALL":
                return BookingMapper.listToBookingDto(bookingRepository.findAllBookingsOwner(ownerId, page));
            case "CURRENT":
                return BookingMapper.listToBookingDto(bookingRepository.findAllCurrentBookingsOwner(ownerId, now, page));
            case "PAST":
                return BookingMapper.listToBookingDto(bookingRepository.findAllPastBookingsOwner(ownerId, now, page));
            case "FUTURE":
                return BookingMapper.listToBookingDto(bookingRepository.findAllFutureBookingsOwner(ownerId, now, page));
            case "WAITING":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllWaitingBookingsOwner(ownerId, now, BookingStatus.WAITING, page));
            case "REJECTED":
                return BookingMapper.listToBookingDto(bookingRepository
                        .findAllRejectedBookingsOwner(ownerId, BookingStatus.REJECTED, page));
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
