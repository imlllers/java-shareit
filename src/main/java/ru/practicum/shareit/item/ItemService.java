package ru.practicum.shareit.item;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto);

    ItemDto getItemById(Long id, Long userId);

    Collection<ItemDto> getOwnerItems(Long userId);

    Collection<ItemDto> findItems(String text);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);
}
