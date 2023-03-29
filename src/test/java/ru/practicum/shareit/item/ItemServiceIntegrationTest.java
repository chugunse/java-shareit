package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemServiceIntegrationTest {
    private final UserService userService;
    private final ItemService itemService;
    private final ItemRepository itemRepository;

    UserDto userDto = new UserDto(null, "testUser", "test@email.com");


//    @Test
//    void createItemWithRequest() {
//        UserDto createdUser = userService.addUser(userDto);
//        ItemRequest createdItemRequest = requestService.createRequest(itemRequestToCreate, 1L);
//
//        ItemDto itemDto = itemService.createItem(itemDtoToCreate, 1L);
//        Item item = itemRepository.findById(itemDto.getId()).orElse(new Item());
//
//        assertThat(item.getId(), equalTo(1L));
//        assertThat(item.getName(), equalTo(itemDtoToCreate.getName()));
//        assertThat(item.getDescription(), equalTo(itemDtoToCreate.getDescription()));
//        assertThat(item.getAvailable(), equalTo(itemDtoToCreate.getAvailable()));
//        assertThat(item.getOwner().getId(), equalTo(createdUser.getId()));
//        assertThat(item.getItemRequest().getId(), equalTo(createdItemRequest.getId()));
//    }
}
