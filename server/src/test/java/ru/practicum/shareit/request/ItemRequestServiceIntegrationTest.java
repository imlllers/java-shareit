package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ItemRequestServiceIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void shouldCreateRequest() {
        User requestor = createUser("Requestor", "requestor-create@test.com");
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need drill");

        ItemRequestDto created = itemRequestService.createRequest(requestor.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need drill");
    }

    @Test
    void shouldReturnRequestByIdWithItems() {
        User requestor = createUser("Requestor", "requestor-id@test.com");
        User owner = createUser("Owner", "owner-id@test.com");
        ItemRequest request = createRequest(requestor, "Need saw");
        createItem(owner, request);

        ItemRequestDto found = itemRequestService.getRequestById(requestor.getId(), request.getId());

        assertThat(found.getId()).isEqualTo(request.getId());
        assertThat(found.getItems()).hasSize(1);
    }

    @Test
    void shouldReturnOwnerRequests() {
        User requestor = createUser("Requestor", "requestor-owner@test.com");
        createRequest(requestor, "Need drill");
        createRequest(requestor, "Need hammer");

        Collection<ItemRequestDto> ownerRequests = itemRequestService.getOwnerRequests(requestor.getId());

        assertThat(ownerRequests).hasSize(2);
    }

    @Test
    void shouldReturnAllRequestsForOtherUser() {
        User requestor = createUser("Requestor", "requestor-all@test.com");
        User viewer = createUser("Viewer", "viewer-all@test.com");
        createRequest(requestor, "Need bike");

        Collection<ItemRequestDto> allRequests = itemRequestService.getAllRequests(viewer.getId());

        assertThat(allRequests).hasSize(1);
    }

    @Test
    void shouldReturnEmptyOwnerRequests() {
        User requestor = createUser("Requestor", "requestor-empty@test.com");

        Collection<ItemRequestDto> ownerRequests = itemRequestService.getOwnerRequests(requestor.getId());

        assertThat(ownerRequests).isEmpty();
    }

    @Test
    void shouldThrowWhenGetRequestByIdForMissingUser() {
        User requestor = createUser("Requestor", "requestor-missing-user@test.com");
        ItemRequest request = createRequest(requestor, "Need drill");

        assertThatThrownBy(() -> itemRequestService.getRequestById(999L, request.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenGetAllRequestsForMissingUser() {
        assertThatThrownBy(() -> itemRequestService.getAllRequests(999L))
                .isInstanceOf(NotFoundException.class);
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private ItemRequest createRequest(User requestor, String description) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(description);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequestRepository.save(itemRequest);
    }

    private Item createItem(User owner, ItemRequest request) {
        Item item = new Item();
        item.setName("Item");
        item.setDescription("Item description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        return itemRepository.save(item);
    }
}
