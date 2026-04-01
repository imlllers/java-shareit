package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ru.practicum.shareit.item.dto.ItemDto addItem(Long userId, ru.practicum.shareit.item.dto.ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        validateItem(itemDto);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        attachRequest(item, itemDto.getRequestId());

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ru.practicum.shareit.item.dto.ItemDto updateItem(Long itemId, Long userId, ru.practicum.shareit.item.dto.ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нет доступа для редактирования вещи");
        }

        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                throw new IllegalArgumentException("Название вещи не может быть пустым");
            }
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                throw new IllegalArgumentException("Описание вещи не может быть пустым");
            }
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        if (itemDto.getRequestId() != null) {
            attachRequest(item, itemDto.getRequestId());
        }

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItemById(Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        return toItemResponseDto(item, userId);
    }

    @Override
    public Collection<ItemDto> getOwnerItems(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        return itemRepository.findByOwnerIdOrderByIdAsc(userId).stream()
                .map(item -> toItemResponseDto(item, userId))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ru.practicum.shareit.item.dto.ItemDto> findItems(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new IllegalArgumentException("Текст комментария не может быть пустым");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        boolean hasBooking = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId,
                itemId,
                Status.APPROVED,
                LocalDateTime.now()
        );

        if (!hasBooking) {
            throw new IllegalArgumentException("Пользователь не может оставить комментарий");
        }

        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private void validateItem(ru.practicum.shareit.item.dto.ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new IllegalArgumentException("Название вещи не может быть пустым");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание вещи не может быть пустым");
        }

        if (itemDto.getAvailable() == null) {
            throw new IllegalArgumentException("Статус доступности вещи должен быть указан");
        }
    }

    private void attachRequest(Item item, Long requestId) {
        if (requestId == null) {
            return;
        }

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос вещи не найден"));
        item.setRequest(itemRequest);
    }

    private ItemDto toItemResponseDto(Item item, Long userId) {
        ItemDto itemResponseDto = ItemMapper.toItemDetailsDto(item);
        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedAsc(item.getId()).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        itemResponseDto.setComments(comments);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            Booking lastBooking = bookingRepository
                    .findTopByItemIdAndStatusAndStartBeforeOrderByStartDesc(item.getId(), Status.APPROVED, now)
                    .orElse(null);
            Booking nextBooking = bookingRepository
                    .findTopByItemIdAndStatusAndStartAfterOrderByStartAsc(item.getId(), Status.APPROVED, now)
                    .orElse(null);

            if (lastBooking != null) {
                itemResponseDto.setLastBooking(
                        ItemMapper.toBookingInfoDto(lastBooking.getId(), lastBooking.getBooker().getId())
                );
            }

            if (nextBooking != null) {
                itemResponseDto.setNextBooking(
                        ItemMapper.toBookingInfoDto(nextBooking.getId(), nextBooking.getBooker().getId())
                );
            }
        }

        return itemResponseDto;
    }
}
