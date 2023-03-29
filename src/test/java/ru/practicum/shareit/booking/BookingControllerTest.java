package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.util.Variables;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    BookingService bookingService;
    @Autowired
    private MockMvc mvc;

    private static final String BASE_PATH_BOOKINGS = "/bookings";
    ItemDto itemDto = ItemDto.builder().name("testItem").description("testDescription").available(true)
            .build();
    BookingDtoShort inputBookingDto = BookingDtoShort.builder()
            .start(LocalDateTime.of(2222, 12, 12, 12, 12, 12))
            .end(LocalDateTime.of(2223, 12, 12, 12, 12, 12))
            .itemId(1L).build();
    BookingDtoShort invalidInputBookingDtoWithWrongStart = BookingDtoShort.builder()
            .start(LocalDateTime.of(1111, 12, 12, 12, 12, 12))
            .end(LocalDateTime.of(2223, 12, 12, 12, 12, 12))
            .itemId(1L).build();
    BookingDto bookingDto = ru.practicum.shareit.booking.dto.BookingDto.builder()
            .start(LocalDateTime.of(2222, 12, 12, 12, 12, 12))
            .end(LocalDateTime.of(2223, 12, 12, 12, 12, 12))
            .item(itemDto)
            .build();

    @Test
    void createValidBooking() throws Exception {
        when(bookingService.addBooking(any(), anyLong()))
                .thenReturn(bookingDto);
        mvc.perform(post(BASE_PATH_BOOKINGS)
                        .header(Variables.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(inputBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDto)));
    }

    @Test
    void createBookingWithWrongStart_shouldReturnStatus400() throws Exception {
        when(bookingService.addBooking(any(), anyLong()))
                .thenReturn(bookingDto);
        mvc.perform(post(BASE_PATH_BOOKINGS)
                        .header(Variables.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(invalidInputBookingDtoWithWrongStart))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingDto);

        mvc.perform(patch(BASE_PATH_BOOKINGS + "/1?approved=true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Variables.HEADER_USER_ID, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDto)));
    }

    @Test
    void getBookingById() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        mvc.perform(get(BASE_PATH_BOOKINGS + "/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Variables.HEADER_USER_ID, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDto)));
    }

    @Test
    void getAllBookingsByUser() throws Exception {
        when(bookingService.getAllBookingsByUser(any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get(BASE_PATH_BOOKINGS + "?state=ALL")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Variables.HEADER_USER_ID, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(bookingDto))));
    }

    @Test
    void getAllUserItemsBookings() throws Exception {
        when(bookingService.gettAllBookingsByOwner(any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get(BASE_PATH_BOOKINGS + "/owner?state=ALL")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Variables.HEADER_USER_ID, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(bookingDto))));
    }
}
