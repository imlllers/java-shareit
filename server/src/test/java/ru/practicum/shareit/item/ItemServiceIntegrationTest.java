package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Autowired
    private BookingService bookingService;

    @Test
    void shouldAttachRequestWhenAddingItem() {
        User savedUser = createUser("Owner", "owner@test.com");

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

    @Test
    void shouldUpdateItemByOwner() {
        User owner = createUser("Owner", "owner-upd@test.com");
        Item item = createItem("Drill", "Powerful drill", true, owner, null);

        ItemDto update = new ItemDto();
        update.setName("New drill");
        update.setDescription("Updated");
        update.setAvailable(false);

        ItemDto updated = itemService.updateItem(item.getId(), owner.getId(), update);

        assertThat(updated.getName()).isEqualTo("New drill");
        assertThat(updated.getDescription()).isEqualTo("Updated");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void shouldGetOwnerItemsWithBookingsAndComments() {
        User owner = createUser("Owner", "owner-items@test.com");
        User booker = createUser("Booker", "booker-items@test.com");
        Item item = createItem("Saw", "Hand saw", true, owner, null);

        BookingDto pastBooking = new BookingDto();
        pastBooking.setItemId(item.getId());
        pastBooking.setStart(LocalDateTime.now().minusDays(3));
        pastBooking.setEnd(LocalDateTime.now().minusDays(2));
        BookingDto createdPastBooking = createApprovedBooking(booker.getId(), owner.getId(), pastBooking);

        BookingDto futureBooking = new BookingDto();
        futureBooking.setItemId(item.getId());
        futureBooking.setStart(LocalDateTime.now().plusDays(2));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto createdFutureBooking = createApprovedBooking(booker.getId(), owner.getId(), futureBooking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Good item");
        itemService.addComment(item.getId(), booker.getId(), commentDto);

        Collection<ItemDto> ownerItems = itemService.getOwnerItems(owner.getId());
        ItemDto responseItem = ownerItems.iterator().next();

        assertThat(ownerItems).hasSize(1);
        assertThat(responseItem.getComments()).hasSize(1);
        assertThat(responseItem.getLastBooking().getId()).isEqualTo(createdPastBooking.getId());
        assertThat(responseItem.getNextBooking().getId()).isEqualTo(createdFutureBooking.getId());
    }

    @Test
    void shouldFindAvailableItemsByText() {
        User owner = createUser("Owner", "owner-search@test.com");
        createItem("Drill", "Powerful drill", true, owner, null);
        createItem("Closed", "Powerful closed", false, owner, null);

        Collection<ItemDto> items = itemService.findItems("powerful");

        assertThat(items).hasSize(1);
        assertThat(items.iterator().next().getName()).isEqualTo("Drill");
    }

    @Test
    void shouldThrowWhenCommentWithoutBooking() {
        User owner = createUser("Owner", "owner-comment@test.com");
        User anotherUser = createUser("Another", "another-comment@test.com");
        Item item = createItem("Drill", "Powerful drill", true, owner, null);
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Nice");

        assertThatThrownBy(() -> itemService.addComment(item.getId(), anotherUser.getId(), commentDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnEmptyWhenSearchTextBlank() {
        Collection<ItemDto> items = itemService.findItems(" ");

        assertThat(items).isEmpty();
    }

    @Test
    void shouldThrowWhenAddItemForMissingUser() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Powerful drill");
        itemDto.setAvailable(true);

        assertThatThrownBy(() -> itemService.addItem(999L, itemDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenUpdateItemByNonOwner() {
        User owner = createUser("Owner", "owner-no-access@test.com");
        User stranger = createUser("Stranger", "stranger-no-access@test.com");
        Item item = createItem("Drill", "Powerful drill", true, owner, null);
        ItemDto patch = new ItemDto();
        patch.setName("Updated");

        assertThatThrownBy(() -> itemService.updateItem(item.getId(), stranger.getId(), patch))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenOwnerItemsForMissingUser() {
        assertThatThrownBy(() -> itemService.getOwnerItems(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldNotSetBookingsForNonOwnerInItemDetails() {
        User owner = createUser("Owner", "owner-details@test.com");
        User booker = createUser("Booker", "booker-details@test.com");
        User viewer = createUser("Viewer", "viewer-details@test.com");
        Item item = createItem("Saw", "Hand saw", true, owner, null);
        BookingDto booking = new BookingDto();
        booking.setItemId(item.getId());
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(2));
        createApprovedBooking(booker.getId(), owner.getId(), booking);

        ItemDto details = itemService.getItemById(item.getId(), viewer.getId());

        assertThat(details.getLastBooking()).isNull();
        assertThat(details.getNextBooking()).isNull();
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Item createItem(String name, String description, boolean available, User owner, ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        item.setRequest(request);
        return itemRepository.save(item);
    }

    private BookingDto createApprovedBooking(Long bookerId, Long ownerId, BookingDto bookingDto) {
        BookingDto created = new BookingDto();
        created.setItemId(bookingDto.getItemId());
        created.setStart(bookingDto.getStart());
        created.setEnd(bookingDto.getEnd());
        BookingDto createdBooking = bookingService.createBooking(bookerId, created);
        return bookingService.updateBookingStatus(ownerId, createdBooking.getId(), true);
    }
}
