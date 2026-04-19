package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(itemRequest.getId());
        itemRequestDto.setDescription(itemRequest.getDescription());
        itemRequestDto.setRequestorId(itemRequest.getRequestor().getId());
        itemRequestDto.setCreated(itemRequest.getCreated());
        return itemRequestDto;
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(itemRequestDto.getId());
        itemRequest.setDescription(itemRequestDto.getDescription());

        if (itemRequestDto.getRequestorId() != null) {
            User requestor = new User();
            requestor.setId(itemRequestDto.getRequestorId());
            itemRequest.setRequestor(requestor);
        }

        itemRequest.setCreated(itemRequestDto.getCreated());
        return itemRequest;
    }

    public static ItemAnswerDto toItemAnswerDto(Item item) {
        ItemAnswerDto itemAnswerDto = new ItemAnswerDto();
        itemAnswerDto.setId(item.getId());
        itemAnswerDto.setName(item.getName());
        itemAnswerDto.setOwnerId(item.getOwner().getId());
        return itemAnswerDto;
    }
}
