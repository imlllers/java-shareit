package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemDto addItem(Integer userId, ItemDto itemDto) {
        User owner = validateUser(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.add(item));
    }

    public ItemDto updateItem(Integer itemId, Integer userId, ItemDto itemDto) {
        Item item = validateItem(itemId);
        validateUser(userId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нет доступа для редактирования вещи");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.update(item));
    }

    public ItemDto getItemById(Integer id) {
        return ItemMapper.toItemDto(validateItem(id));
    }

    public Collection<ItemDto> getOwnerItems(Integer userId) {
        return itemRepository.getOwnerItems(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public Collection<ItemDto> findItems(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        return itemRepository.find(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private User validateUser(Integer userId) {
        User user = userRepository.getById(userId);

        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        return user;
    }

    private Item validateItem(Integer id) {
        Item item = itemRepository.getById(id);

        if (item == null) {
            throw new NotFoundException("Вещь не найдена");
        }

        return item;
    }
}
