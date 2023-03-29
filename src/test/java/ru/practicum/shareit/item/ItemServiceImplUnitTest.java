package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.model.AccessException;
import ru.practicum.shareit.exceptions.model.BadRequestException;
import ru.practicum.shareit.exceptions.model.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplUnitTest {
    @InjectMocks
    ItemServiceImpl itemService;
    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserService userService;
    @Mock
    CommentRepository commentRepository;
    @Mock
    RequestService requestService;

    UserDto userDto = new UserDto(1L, "testUser", "test@email.com");
    User user = new User(1L, "testUser", "test@email.com");
    ItemRequest itemRequest = ItemRequest.builder()
            .id(1L)
            .description("testDescription")
            .requester(user)
            .items(new ArrayList<>())
            .build();
    ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(1L)
            .description("testDescription")
            .requester(userDto)
            .items(new ArrayList<>())
            .build();
    Item item = Item.builder()
            .id(1L)
            .name("testItem")
            .description("testDescription")
            .available(true)
            .itemRequest(itemRequest)
            .ownerId(1L)
            .build();
    ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("testItem")
            .description("testDescription")
            .available(true)
            .requestId(1L)
            .build();
    ItemDto itemDtoUpdate = ItemDto.builder()
            .id(1L)
            .name("testItemUpdate")
            .description("testDescriptionUpdate")
            .available(true)
            .requestId(1L)
            .build();

    List<Booking> bookingList = List.of(Booking.builder()
                    .id(1L).item(item).booker(user)
                    .start(LocalDateTime.now().minusHours(2L))
                    .end(LocalDateTime.now().minusHours(1L))
                    .status(BookingStatus.WAITING).build(),
            Booking.builder()
                    .id(2L).item(item).booker(user)
                    .start(LocalDateTime.now().plusHours(1L))
                    .end(LocalDateTime.now().plusHours(2L))
                    .status(BookingStatus.WAITING).build());
    Comment comment = Comment.builder().id(1L).text("testText").item(item).author(user).build();
    CommentDto commentDto = CommentDto.builder().id(1L).text("testText").item(itemDto).authorName("testUser").build();

    @Test
    void addItemTest() {
        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);
        when(requestService.getById(anyLong(), anyLong()))
                .thenReturn(itemRequestDto);
        when(itemRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));
        assertThat(itemService.addItem(itemDto, 1L), equalTo(itemDto));
    }

    @Test
    public void getItemById_shouldReturnItemDtoWithBookingsWhenOwnerRequestItem() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        when(commentRepository.findAllByItem_Id(anyLong())).thenReturn(List.of(comment));

        when(bookingRepository.findAllBookingsItem(anyLong()))
                .thenReturn(bookingList);
        ItemDto requestedItemDto = itemService.getItemById(1L, 1L);

        assertThat(requestedItemDto.getName(), equalTo(item.getName()));
        assertThat(requestedItemDto.getDescription(), equalTo(item.getDescription()));
        assertThat(requestedItemDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(requestedItemDto.getComments(), hasSize(1));
        assertThat(requestedItemDto.getLastBooking().getId(), equalTo(1L));
        assertThat(requestedItemDto.getLastBooking().getBookerId(), equalTo(1L));
        assertThat(requestedItemDto.getNextBooking().getId(), equalTo(2L));
        assertThat(requestedItemDto.getNextBooking().getBookerId(), equalTo(1L));
    }

    @Test
    public void getItemById_shouldReturnItemDtoWithoutBookingsWhenNotOwnerRequestItem() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        when(commentRepository.findAllByItem_Id(anyLong())).thenReturn(List.of(comment));

        ItemDto requestedItemDto = itemService.getItemById(1L, 2L);

        assertThat(requestedItemDto.getName(), equalTo(item.getName()));
        assertThat(requestedItemDto.getDescription(), equalTo(item.getDescription()));
        assertThat(requestedItemDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(requestedItemDto.getComments(), hasSize(1));
        assertThat(requestedItemDto.getLastBooking(), nullValue());
        assertThat(requestedItemDto.getNextBooking(), nullValue());
    }

    @Test
    void getItemById_shouldThrowNotFoundExceptionWhenItemIsNotExist() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception e = Assertions.assertThrows(NotFoundException.class, () -> itemService
                .getItemById(1L, 1L));
        assertThat(e.getMessage(), equalTo("item по id 1 не найден"));
    }

    @Test
    void getAllUserItems_shouldReturnItem() {
        when(itemRepository.findAllByOwnerId(anyLong(), any())).thenReturn(List.of(item));
        when(bookingRepository.findAllBookingsItem(anyLong()))
                .thenReturn(bookingList);

        List<ItemDto> userItemsList = itemService.getAllUsersItems(1L, 0, 10);

        assertThat(userItemsList, hasSize(1));
        assertThat(userItemsList.get(0).getLastBooking().getId(), equalTo(1L));
        assertThat(userItemsList.get(0).getLastBooking().getBookerId(), equalTo(1L));
        assertThat(userItemsList.get(0).getNextBooking().getId(), equalTo(2L));
        assertThat(userItemsList.get(0).getNextBooking().getBookerId(), equalTo(1L));
    }

    @Test
    void editItem_shouldThrowNotFoundExceptionWhenItemIsNotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception e = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.updateItem(itemDto, 1L, 1L));
        assertThat(e.getMessage(), equalTo("item по id 1 не найден"));
    }

    @Test
    void editItem_shouldThrowUserIsNotOwnerExceptionWhenNotOwnerRequestUpdate() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Exception e = Assertions.assertThrows(AccessException.class,
                () -> itemService.updateItem(itemDto, 1L, 2L));
        assertThat(e.getMessage(), equalTo("пользователь с id = 2 не имеет доступа правки item с id = 1"));
    }

    @Test
    void editItemTest() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);
        when(itemRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));
        assertThat(itemService.updateItem(itemDtoUpdate, 1L, 1L), equalTo(itemDtoUpdate));
    }

    @Test
    void searchAvailableItems() {
        assertThat(itemService.searchAvailableItems("", 0, 10), hasSize(0));
        assertThat(itemService.searchAvailableItems(null, 0, 10), hasSize(0));
        when(itemRepository.searchAvailableItems(anyString(), any()))
                .thenReturn(List.of(item));
        assertThat(itemService.searchAvailableItems("item", 0, 10), equalTo(List.of(itemDto)));
    }

    @Test
    void addCommentTest() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);
        when(bookingRepository.findAllByItem_IdAndBooker_IdAndStatusIsAndEndIsBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(bookingList);
        when(commentRepository.save(any()))
                .thenReturn(comment);
        when(commentRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));
        CommentDto testComment = itemService.addComment(1L, 1L, commentDto);
        assertThat(testComment.getId(), equalTo(commentDto.getId()));
        assertThat(testComment.getItem(), equalTo(commentDto.getItem()));
        assertThat(testComment.getText(), equalTo(commentDto.getText()));
        assertThat(testComment.getAuthorName(), equalTo(commentDto.getAuthorName()));
    }

    @Test
    void addCommentBookingsEmptyTest() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);
        when(bookingRepository
                .findAllByItem_IdAndBooker_IdAndStatusIsAndEndIsBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        Exception e = Assertions.assertThrows(BadRequestException.class,
                () -> itemService.addComment(1L, 1L, commentDto));
        assertThat(e.getMessage(), equalTo("у юзара testUser нет завершенных бронирований TESTITEM"));
    }
}
