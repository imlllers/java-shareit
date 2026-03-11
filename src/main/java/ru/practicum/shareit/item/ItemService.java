package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto addItem(Integer userId, ItemDto itemDto);

    ItemDto updateItem(Integer itemId, Integer userId, ItemDto itemDto);

    ItemDto getItemById(Integer id);

    Collection<ItemDto> getOwnerItems(Integer userId);

    Collection<ItemDto> findItems(String text);
}
