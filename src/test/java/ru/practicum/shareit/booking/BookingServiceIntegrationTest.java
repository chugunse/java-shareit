package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingServiceIntegrationTest {
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;
    UserDto owner = new UserDto(null, "testUser", "test@email.com");
    UserDto booker = new UserDto(null, "testUser2", "test2@email.com");
    ItemDto itemDtoToCreate = ItemDto.builder().name("testItem").description("testDescription").available(true).build();
    BookingDtoShort bookingToCreate = BookingDtoShort.builder().itemId(1L).start(LocalDateTime.now().plusHours(1))
            .end(LocalDateTime.now().plusHours(2)).build();

    @Test
    void createBooking() {
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        ItemDto itemDto = itemService.addItem(itemDtoToCreate, 1L);

        BookingDto createdBooking = bookingService.addBooking(bookingToCreate, createdBooker.getId());

        assertThat(createdBooking.getId(), equalTo(1L));
        assertThat(createdBooking.getStart(), equalTo(bookingToCreate.getStart()));
        assertThat(createdBooking.getEnd(), equalTo(bookingToCreate.getEnd()));
        assertThat(createdBooking.getBooker(), equalTo(createdBooker));
        assertThat(createdBooking.getItem().getId(), equalTo(itemDto.getId()));
        assertThat(createdBooking.getStatus(), equalTo(BookingStatus.WAITING));
    }

    @Test
    void getBookingById() {
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        ItemDto itemDto = itemService.addItem(itemDtoToCreate, 1L);
        bookingService.addBooking(bookingToCreate, createdBooker.getId());

        BookingDto returnedBooking = bookingService.getBookingById(1L, 2L);

        assertThat(returnedBooking.getId(), equalTo(1L));
        assertThat(returnedBooking.getStart(), equalTo(bookingToCreate.getStart()));
        assertThat(returnedBooking.getEnd(), equalTo(bookingToCreate.getEnd()));
        assertThat(returnedBooking.getBooker(), equalTo(createdBooker));
        assertThat(returnedBooking.getItem().getId(), equalTo(itemDto.getId()));
        assertThat(returnedBooking.getStatus(), equalTo(BookingStatus.WAITING));
    }

    @Test
    void approveBooking_approve() {
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        ItemDto itemDto = itemService.addItem(itemDtoToCreate, 1L);
        bookingService.addBooking(bookingToCreate, createdBooker.getId());
        BookingDto approvedBooking = bookingService.approve(1L, 1L, true);

        assertThat(approvedBooking.getId(), equalTo(1L));
        assertThat(approvedBooking.getStart(), equalTo(bookingToCreate.getStart()));
        assertThat(approvedBooking.getEnd(), equalTo(bookingToCreate.getEnd()));
        assertThat(approvedBooking.getBooker(), equalTo(createdBooker));
        assertThat(approvedBooking.getItem().getId(), equalTo(itemDto.getId()));
        assertThat(approvedBooking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void approveBooking_reject() {
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        ItemDto itemDto = itemService.addItem(itemDtoToCreate, 1L);
        bookingService.addBooking(bookingToCreate, createdBooker.getId());
        BookingDto approvedBooking = bookingService.approve(1L, 1L, false);

        assertThat(approvedBooking.getId(), equalTo(1L));
        assertThat(approvedBooking.getStart(), equalTo(bookingToCreate.getStart()));
        assertThat(approvedBooking.getEnd(), equalTo(bookingToCreate.getEnd()));
        assertThat(approvedBooking.getBooker(), equalTo(createdBooker));
        assertThat(approvedBooking.getItem().getId(), equalTo(itemDto.getId()));
        assertThat(approvedBooking.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void getAllBookingsByUser_ALL() {
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(itemDtoToCreate, 1L);
        BookingDto bookingDto = bookingService.addBooking(bookingToCreate, createdBooker.getId());

        List<BookingDto> userBookingsList = bookingService.getAllBookingsByUser("ALL", 2L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @Test
    void getAllBookingsByUser_CURRENT() {
        BookingDtoShort booking = BookingDtoShort.builder().itemId(1L)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(2)).build();
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(itemDtoToCreate, 1L);
        BookingDto bookingDto = bookingService.addBooking(booking, createdBooker.getId());

        List<BookingDto> userBookingsList = bookingService.getAllBookingsByUser("CURRENT", 2L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @Test
    void getAllBookingsByUser_PAST() {
        BookingDtoShort booking = BookingDtoShort.builder().itemId(1L)
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1)).build();
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(itemDtoToCreate, 1L);
        BookingDto bookingDto = bookingService.addBooking(booking, createdBooker.getId());

        List<BookingDto> userBookingsList = bookingService.getAllBookingsByUser("PAST", 2L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @Test
    void getAllBookingsByUser_FUTURE() {
        BookingDtoShort booking = BookingDtoShort.builder().itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2)).build();
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(itemDtoToCreate, 1L);
        BookingDto bookingDto = bookingService.addBooking(booking, createdBooker.getId());

        List<BookingDto> userBookingsList = bookingService.getAllBookingsByUser("FUTURE", 2L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @Test
    void getAllBookingsByUser_WAITING() {
        BookingDtoShort booking = BookingDtoShort.builder().itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2)).build();
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(itemDtoToCreate, 1L);
        BookingDto bookingDto = bookingService.addBooking(booking, createdBooker.getId());

        List<BookingDto> userBookingsList = bookingService.getAllBookingsByUser("WAITING", 2L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @Test
    void getAllBookingsByUser_REJECTED() {
        BookingDtoShort booking = BookingDtoShort.builder().itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2)).build();
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(itemDtoToCreate, 1L);
        BookingDto bookingDto = bookingService.addBooking(booking, createdBooker.getId());
        bookingService.approve(bookingDto.getId(), 1L, false);

        List<BookingDto> userBookingsList = bookingService.getAllBookingsByUser("REJECTED", 2L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @Test
    void getAllBookingsByOwner_ALL() {
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(itemDtoToCreate, 1L);
        BookingDto bookingDto = bookingService.addBooking(bookingToCreate, createdBooker.getId());

        List<BookingDto> userBookingsList = bookingService.gettAllBookingsByOwner("ALL", 1L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @Test
    void getAllBookingsByOwner_CURRENT() {
        BookingDtoShort booking = BookingDtoShort.builder().itemId(1L)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(2)).build();
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(itemDtoToCreate, 1L);
        BookingDto bookingDto = bookingService.addBooking(booking, createdBooker.getId());

        List<BookingDto> userBookingsList = bookingService.gettAllBookingsByOwner("CURRENT", 1L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @Test
    void getAllBookingsByOwner_PAST() {
        BookingDtoShort booking = BookingDtoShort.builder().itemId(1L)
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1)).build();
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(itemDtoToCreate, 1L);
        BookingDto bookingDto = bookingService.addBooking(booking, createdBooker.getId());

        List<BookingDto> userBookingsList = bookingService.gettAllBookingsByOwner("PAST", 1L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @Test
    void getAllBookingsByOwner_FUTURE() {
        BookingDtoShort booking = BookingDtoShort.builder().itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2)).build();
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(itemDtoToCreate, 1L);
        BookingDto bookingDto = bookingService.addBooking(booking, createdBooker.getId());

        List<BookingDto> userBookingsList = bookingService.gettAllBookingsByOwner("FUTURE", 1L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @Test
    void getAllBookingsByOwner_WAITING() {
        BookingDtoShort booking = BookingDtoShort.builder().itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2)).build();
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(itemDtoToCreate, 1L);
        BookingDto bookingDto = bookingService.addBooking(booking, createdBooker.getId());

        List<BookingDto> userBookingsList = bookingService.gettAllBookingsByOwner("WAITING", 1L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @Test
    void getAllBookingsByOwner_REJECTED() {
        BookingDtoShort booking = BookingDtoShort.builder().itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2)).build();
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(itemDtoToCreate, 1L);
        BookingDto bookingDto = bookingService.addBooking(booking, createdBooker.getId());
        bookingService.approve(bookingDto.getId(), 1L, false);

        List<BookingDto> userBookingsList = bookingService.gettAllBookingsByOwner("REJECTED", 1L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }
}
