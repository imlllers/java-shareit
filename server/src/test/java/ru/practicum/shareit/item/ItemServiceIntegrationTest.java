package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ItemServiceIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void shouldAttachRequestWhenAddingItem() {
        User user = new User();
        user.setName("Owner");
        user.setEmail("owner@test.com");
        User savedUser = userRepository.save(user);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Need drill");
        itemRequest.setRequestor(savedUser);
        itemRequest.setCreated(LocalDateTime.now());
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Powerful drill");
        itemDto.setAvailable(true);
        itemDto.setRequestId(savedRequest.getId());

        ItemDto savedItemDto = itemService.addItem(savedUser.getId(), itemDto);
        Item savedItem = itemRepository.findById(savedItemDto.getId()).orElseThrow();

        assertThat(savedItemDto.getRequestId()).isEqualTo(savedRequest.getId());
        assertThat(savedItem.getRequest()).isNotNull();
        assertThat(savedItem.getRequest().getId()).isEqualTo(savedRequest.getId());
    }
}
