package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.util.Variables;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    RequestService requestService;
    @Autowired
    private MockMvc mvc;

    private static final String BASE_PATH_REQUESTS = "/requests";
    ItemRequestDto itemRequestToCreate = ItemRequestDto.builder().description("testDescription").build();
    ItemRequestDto itemRequestToBack = ItemRequestDto.builder().id(1L).description("testDescription").build();
    ItemRequestDto badItemRequestToCreate = ItemRequestDto.builder().description("").build();

    @Test
    void addUserTest() throws Exception {
        when(requestService.addRequest(anyLong(), any()))
                .thenReturn(itemRequestToBack);

        mvc.perform(post(BASE_PATH_REQUESTS)
                        .header(Variables.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(itemRequestToCreate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestToCreate.getDescription())));
    }

    @Test
    void createInvalidRequestWithEmptyDescription_shouldReturnStatus400() throws Exception {
        when(requestService.addRequest(anyLong(), any()))
                .thenReturn(itemRequestToBack);
        mvc.perform(post(BASE_PATH_REQUESTS)
                        .header(Variables.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(badItemRequestToCreate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUserRequests() throws Exception {
        when(requestService.getAllUserRequest(anyLong()))
                .thenReturn(List.of(itemRequestToBack));
        mvc.perform(get(BASE_PATH_REQUESTS)
                        .header(Variables.HEADER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(itemRequestToBack))));
    }

    @Test
    void getAllRequests() throws Exception {
        when(requestService.getAllRequest(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestToBack));
        mvc.perform(get(BASE_PATH_REQUESTS + "/all")
                        .header(Variables.HEADER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(itemRequestToBack))));
    }

    @Test
    void getRequestById() throws Exception {
        when(requestService.getById(anyLong(), anyLong()))
                .thenReturn(itemRequestToBack);
        mvc.perform(get(BASE_PATH_REQUESTS + "/1")
                        .header(Variables.HEADER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemRequestToBack)));
    }
}
