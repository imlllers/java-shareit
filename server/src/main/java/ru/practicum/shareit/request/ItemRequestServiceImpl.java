package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        return mapRequestWithItems(itemRequestRepository.save(itemRequest), Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        validateUserExists(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос вещи не найден"));

        List<Item> items = itemRepository.findByRequestIdInOrderByIdAsc(List.of(requestId));
        return mapRequestWithItems(itemRequest, items);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemRequestDto> getOwnerRequests(Long userId) {
        validateUserExists(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        return mapRequestsWithItems(itemRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemRequestDto> getAllRequests(Long userId) {
        validateUserExists(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId);
        return mapRequestsWithItems(itemRequests);
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private Collection<ItemRequestDto> mapRequestsWithItems(List<ItemRequest> itemRequests) {
        if (itemRequests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdInOrderByIdAsc(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return itemRequests.stream()
                .map(itemRequest -> mapRequestWithItems(
                        itemRequest,
                        itemsByRequestId.getOrDefault(itemRequest.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    private ItemRequestDto mapRequestWithItems(ItemRequest itemRequest, List<Item> items) {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setItems(items.stream()
                .map(ItemRequestMapper::toItemAnswerDto)
                .collect(Collectors.toList()));
        return itemRequestDto;
    }
}
