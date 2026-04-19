package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto);

    ItemRequestDto getRequestById(Long userId, Long requestId);

    Collection<ItemRequestDto> getOwnerRequests(Long userId);

    Collection<ItemRequestDto> getAllRequests(Long userId);
}
