package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.util.*;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.model.AccessException;
import ru.practicum.shareit.exceptions.model.BadRequestException;
import ru.practicum.shareit.exceptions.model.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.shareit.util.ItemMapper.toItem;
import static ru.practicum.shareit.util.ItemMapper.toItemDto;

@Service
@AllArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    private final CommentRepository commentRepository;

    private final RequestService requestService;

    @Transactional
    @Override
    public ItemDto addItem(ItemDto itemDto, Long userId) {
        userService.getUserById(userId);
        Item item = toItem(itemDto);
        item.setOwnerId(userId);
        item.setItemRequest(itemDto.getRequestId() != null ?
                RequestMapper.fromDto(requestService.getById(userId, itemDto.getRequestId())) : null);
        return toItemDto(itemRepository.save(item));
    }

    @Transactional
    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        ItemDto result;
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("item по id %d не найден", itemId)));
        result = ItemMapper.toItemDto(item);
        if (Objects.equals(item.getOwnerId(), userId)) {
            setBookings(result);
        }
        setCommet(result);
        return result;
    }

    public ItemDto setBookings(ItemDto itemDto) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findAllBookingsItem(itemDto.getId());
        Booking lastBooking = bookings.stream()
                .filter(obj -> !(obj.getStatus().equals(BookingStatus.REJECTED)))
                .filter(obj -> obj.getStart().isBefore(now))
                .min((obj1, obj2) -> obj2.getStart().compareTo(obj1.getStart())).orElse(null);
        Booking nextBooking = bookings.stream()
                .filter(obj -> !(obj.getStatus().equals(BookingStatus.REJECTED)))
                .filter(obj -> obj.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart)).orElse(null);
        if (lastBooking != null) {
            itemDto.setLastBooking(BookingMapper.toItemBookingDto(lastBooking));
        }
        if (nextBooking != null) {
            itemDto.setNextBooking(BookingMapper.toItemBookingDto(nextBooking));
        }
        return itemDto;
    }

    @Transactional
    @Override
    public List<ItemDto> getAllUsersItems(Long userId, int from, int size) {
        Sort sortByCreated = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortByCreated);
        List<ItemDto> item = itemRepository.findAllByOwnerId(userId, page).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        return item.stream()
                .map(this::setBookings)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ItemDto updateItem(ItemDto itemDto, Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("item по id %d не найден", itemId)));
        userService.getUserById(userId);
        if (!item.getOwnerId().equals(userId)) {
            throw new AccessException(String
                    .format("пользователь с id = %d не имеет доступа правки item с id = %d", userId, itemId));
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return toItemDto(itemRepository.save(item));
    }

    @Transactional
    @Override
    public List<ItemDto> searchAvailableItems(String text, int from, int size) {
        Pageable page = PageRequest.of(from / size, size);
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.searchAvailableItems(text, page).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public Long getOwnerId(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("item по id %d не найден", itemId)))
                .getOwnerId();
    }

    @Transactional
    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("item по id %d не найден", itemId)));
        User user = UserMapper.toUser(userService.getUserById(userId));
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = bookingRepository
                .findAllByItem_IdAndBooker_IdAndStatusIsAndEndIsBefore(itemId, userId, BookingStatus.APPROVED, now);
        if (bookings.isEmpty()) {
            throw new BadRequestException(String
                    .format("у юзара %s нет завершенных бронирований %S", user.getName(), item.getName()));
        }
        if (bookings.get(0).getStart().isBefore(now)) {
            Comment comment = CommentMapper.toComment(commentDto);
            comment.setItem(item);
            comment.setAuthor(user);
            comment.setCreated(now);
            return CommentMapper.toDto(commentRepository.save(comment));
        } else {
            throw new BadRequestException(String
                    .format("у юзара %s нет завершенных бронирований %S", user.getName(), item.getName()));
        }
    }

    public void setCommet(ItemDto itemDto) {
        List<Comment> list = commentRepository.findAllByItem_Id(itemDto.getId());
        itemDto.setComments(CommentMapper.toDtoList(list));
    }
}
