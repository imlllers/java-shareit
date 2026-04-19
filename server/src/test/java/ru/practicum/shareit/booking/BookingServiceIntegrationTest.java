package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void shouldCreateBooking() {
        User owner = createUser("Owner", "owner-booking@test.com");
        User booker = createUser("Booker", "booker-booking@test.com");
        Item item = createItem(owner, true);
        BookingDto bookingDto = createBookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        BookingDto created = bookingService.createBooking(booker.getId(), bookingDto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    void shouldApproveBooking() {
        User owner = createUser("Owner", "owner-approve@test.com");
        User booker = createUser("Booker", "booker-approve@test.com");
        Item item = createItem(owner, true);
        BookingDto created = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))
        );

        BookingDto approved = bookingService.updateBookingStatus(owner.getId(), created.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void shouldReturnBookingByIdForOwner() {
        User owner = createUser("Owner", "owner-getbooking@test.com");
        User booker = createUser("Booker", "booker-getbooking@test.com");
        Item item = createItem(owner, true);
        BookingDto created = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))
        );

        BookingDto found = bookingService.getBookingById(owner.getId(), created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
    }

    @Test
    void shouldReturnUserBookingsByState() {
        User owner = createUser("Owner", "owner-userstate@test.com");
        User booker = createUser("Booker", "booker-userstate@test.com");
        Item item = createItem(owner, true);
        bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))
        );

        Collection<BookingDto> waiting = bookingService.getUserBookings(booker.getId(), "WAITING");

        assertThat(waiting).hasSize(1);
    }

    @Test
    void shouldReturnOwnerBookingsByState() {
        User owner = createUser("Owner", "owner-ownerstate@test.com");
        User booker = createUser("Booker", "booker-ownerstate@test.com");
        Item item = createItem(owner, true);
        BookingDto created = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2))
        );
        bookingRepository.findById(created.getId()).ifPresent(booking -> {
            booking.setStatus(Status.APPROVED);
            bookingRepository.save(booking);
        });

        Collection<BookingDto> past = bookingService.getOwnerBookings(owner.getId(), "PAST");

        assertThat(past).hasSize(1);
    }

    @Test
    void shouldThrowWhenNonOwnerUpdatesBooking() {
        User owner = createUser("Owner", "owner-forbidden@test.com");
        User booker = createUser("Booker", "booker-forbidden@test.com");
        User stranger = createUser("Stranger", "stranger-forbidden@test.com");
        Item item = createItem(owner, true);
        BookingDto created = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))
        );

        assertThatThrownBy(() -> bookingService.updateBookingStatus(stranger.getId(), created.getId(), true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowWhenBookingStatusAlreadySet() {
        User owner = createUser("Owner", "owner-conflict@test.com");
        User booker = createUser("Booker", "booker-conflict@test.com");
        Item item = createItem(owner, true);
        BookingDto created = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))
        );
        bookingService.updateBookingStatus(owner.getId(), created.getId(), true);

        assertThatThrownBy(() -> bookingService.updateBookingStatus(owner.getId(), created.getId(), true))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldThrowWhenOwnerBooksOwnItem() {
        User owner = createUser("Owner", "owner-own-item@test.com");
        Item item = createItem(owner, true);
        BookingDto bookingDto = createBookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createBooking(owner.getId(), bookingDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnAllUserStates() {
        User owner = createUser("Owner", "owner-user-states@test.com");
        User booker = createUser("Booker", "booker-user-states@test.com");
        Item item = createItem(owner, true);

        BookingDto current = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1))
        );
        BookingDto past = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2))
        );
        BookingDto future = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4))
        );
        bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(6))
        );
        BookingDto rejected = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().plusDays(7), LocalDateTime.now().plusDays(8))
        );

        bookingRepository.findById(current.getId()).ifPresent(booking -> booking.setStatus(Status.APPROVED));
        bookingRepository.findById(past.getId()).ifPresent(booking -> booking.setStatus(Status.APPROVED));
        bookingRepository.findById(future.getId()).ifPresent(booking -> booking.setStatus(Status.APPROVED));
        bookingRepository.findById(rejected.getId()).ifPresent(booking -> booking.setStatus(Status.REJECTED));

        assertThat(bookingService.getUserBookings(booker.getId(), "ALL")).hasSize(5);
        assertThat(bookingService.getUserBookings(booker.getId(), "CURRENT")).hasSize(1);
        assertThat(bookingService.getUserBookings(booker.getId(), "PAST")).hasSize(1);
        assertThat(bookingService.getUserBookings(booker.getId(), "FUTURE")).hasSize(3);
        assertThat(bookingService.getUserBookings(booker.getId(), "WAITING")).hasSize(1);
        assertThat(bookingService.getUserBookings(booker.getId(), "REJECTED")).hasSize(1);
    }

    @Test
    void shouldReturnAllOwnerStates() {
        User owner = createUser("Owner", "owner-owner-states@test.com");
        User booker = createUser("Booker", "booker-owner-states@test.com");
        Item item = createItem(owner, true);

        BookingDto current = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1))
        );
        BookingDto past = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(3))
        );
        BookingDto future = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(5))
        );
        bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(7))
        );
        BookingDto rejected = bookingService.createBooking(
                booker.getId(),
                createBookingDto(item.getId(), LocalDateTime.now().plusDays(8), LocalDateTime.now().plusDays(9))
        );

        bookingRepository.findById(current.getId()).ifPresent(booking -> booking.setStatus(Status.APPROVED));
        bookingRepository.findById(past.getId()).ifPresent(booking -> booking.setStatus(Status.APPROVED));
        bookingRepository.findById(future.getId()).ifPresent(booking -> booking.setStatus(Status.APPROVED));
        bookingRepository.findById(rejected.getId()).ifPresent(booking -> booking.setStatus(Status.REJECTED));

        assertThat(bookingService.getOwnerBookings(owner.getId(), "ALL")).hasSize(5);
        assertThat(bookingService.getOwnerBookings(owner.getId(), "CURRENT")).hasSize(1);
        assertThat(bookingService.getOwnerBookings(owner.getId(), "PAST")).hasSize(1);
        assertThat(bookingService.getOwnerBookings(owner.getId(), "FUTURE")).hasSize(3);
        assertThat(bookingService.getOwnerBookings(owner.getId(), "WAITING")).hasSize(1);
        assertThat(bookingService.getOwnerBookings(owner.getId(), "REJECTED")).hasSize(1);
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Item createItem(User owner, boolean available) {
        Item item = new Item();
        item.setName("Item");
        item.setDescription("Item description");
        item.setAvailable(available);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    private BookingDto createBookingDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        return bookingDto;
    }
}
