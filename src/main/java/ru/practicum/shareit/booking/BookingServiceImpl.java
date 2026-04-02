package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final Sort newestFirst = Sort.by(Sort.Direction.DESC, "start");

    @Override
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        validateBookingDates(bookingDto);

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }

        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto updateBookingStatus(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Нет доступа к изменению бронирования");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ConflictException("Статус бронирования уже установлен");
        }

        booking.setStatus(Boolean.TRUE.equals(approved) ? Status.APPROVED : Status.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Бронирование не найдено");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<BookingDto> getUserBookings(Long userId, String state) {
        checkUserExists(userId);
        return getBookingsForBooker(userId, BookingState.from(state)).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<BookingDto> getOwnerBookings(Long userId, String state) {
        checkUserExists(userId);
        return getBookingsForOwner(userId, BookingState.from(state)).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private void validateBookingDates(BookingDto bookingDto) {
        if (!bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            throw new IllegalArgumentException("Дата окончания должна быть позже даты начала");
        }
    }

    private List<Booking> getBookingsForBooker(Long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case ALL -> bookingRepository.findByBookerId(userId, newestFirst);
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(userId, now, now, newestFirst);
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(userId, now, newestFirst);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(userId, now, newestFirst);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING, newestFirst);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED, newestFirst);
        };
    }

    private List<Booking> getBookingsForOwner(Long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case ALL -> bookingRepository.findByItemOwnerId(userId, newestFirst);
            case CURRENT -> bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(userId, now, now, newestFirst);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBefore(userId, now, newestFirst);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfter(userId, now, newestFirst);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatus(userId, Status.WAITING, newestFirst);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatus(userId, Status.REJECTED, newestFirst);
        };
    }
}
