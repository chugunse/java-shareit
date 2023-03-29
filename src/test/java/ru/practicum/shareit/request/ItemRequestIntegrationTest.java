package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.RequestMapper;

import javax.transaction.Transactional;
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
public class ItemRequestIntegrationTest {
    private final RequestService requestService;
    private final ItemRepository itemRepository;
    private final UserService userService;

    UserDto requester = new UserDto(null, "testUser", "test@email.com");
    UserDto itemOwner = new UserDto(null, "testOwner", "Owner@email.com");
    ItemRequestDto itemRequestToCreate = ItemRequestDto.builder().description("testDescr").build();

    @Test
    void createRequest() {
        UserDto createdUser = userService.addUser(requester);

        ItemRequestDto createdItemRequest = requestService.addRequest(1L, itemRequestToCreate);

        assertThat(createdItemRequest.getId(), equalTo(1L));
        assertThat(createdItemRequest.getDescription(), equalTo(itemRequestToCreate.getDescription()));
        assertThat(createdItemRequest.getRequester(), equalTo(createdUser));
    }

    @Test
    void getRequestById() {
        userService.addUser(requester);
        userService.addUser(itemOwner);
        ItemRequestDto createdItemRequest = requestService.addRequest(1L, itemRequestToCreate);
        Item item = Item.builder().name("testName").description("testDescription")
                .itemRequest(RequestMapper.fromDto(createdItemRequest))
                .available(true).ownerId(2L).build();
        itemRepository.save(item);

        ItemRequestDto itemRequest = requestService.getById(1L, 1L);

        assertThat(itemRequest.getId(), equalTo(1L));
    }

    @Test
    void getAllUserRequests() {
        userService.addUser(requester);
        userService.addUser(itemOwner);
        ItemRequestDto createdItemRequest = requestService.addRequest(1L, itemRequestToCreate);
        Item item = Item.builder().name("testName").description("testDescription")
                .itemRequest(RequestMapper.fromDto(createdItemRequest))
                .available(true).ownerId(2L).build();
        itemRepository.save(item);

        List<ItemRequestDto> userRequestsList = requestService.getAllUserRequest(1L);

        assertThat(userRequestsList, hasSize(1));
        assertThat(userRequestsList.get(0).getId(), equalTo(1L));
        assertThat(userRequestsList.get(0).getItems(), hasSize(1));
    }

    @Test
    void getAllRequests_shouldReturnEmptyListWhenRequesterFindRequests() {
        userService.addUser(requester);
        userService.addUser(itemOwner);
        ItemRequestDto createdItemRequest = requestService.addRequest(1L, itemRequestToCreate);
        Item item = Item.builder().name("testName").description("testDescription")
                .itemRequest(RequestMapper.fromDto(createdItemRequest))
                .available(true).ownerId(2L).build();
        itemRepository.save(item);

        List<ItemRequestDto> requestsList = requestService.getAllRequest(1L, 0, 10);

        assertThat(requestsList, hasSize(0));
    }

    @Test
    void getAllRequests_shouldReturnListWhenNotRequesterFindRequests() {
        userService.addUser(requester);
        userService.addUser(itemOwner);
        ItemRequestDto createdItemRequest = requestService.addRequest(1L, itemRequestToCreate);
        Item item = Item.builder().name("testName").description("testDescription")
                .itemRequest(RequestMapper.fromDto(createdItemRequest))
                .available(true).ownerId(2L).build();
        itemRepository.save(item);

        List<ItemRequestDto> requestsList = requestService.getAllRequest(2L, 0, 10);

        assertThat(requestsList, hasSize(1));
        assertThat(requestsList.get(0).getId(), equalTo(1L));
    }
}
