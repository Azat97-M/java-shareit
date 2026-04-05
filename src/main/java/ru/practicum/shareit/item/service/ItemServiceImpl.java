package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errorHandler.NotFoundException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = getUserOrThrow(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.create(userId, item));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        getUserOrThrow(userId);
        Item existingItem = getItemOrThrow(itemId);

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Вещь с id " + itemId + " не принадлежит пользователю с id " + userId);
        }

        Item itemUpdate = ItemMapper.toItem(itemDto);
        return ItemMapper.toItemDto(itemRepository.update(userId, itemId, itemUpdate));
    }

    @Override
    public ItemDto findById(Long itemId) {
        return ItemMapper.toItemDto(getItemOrThrow(itemId));
    }

    @Override
    public List<ItemDto> findByOwner(Long userId) {
        getUserOrThrow(userId);
        return itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private User getUserOrThrow(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    private Item getItemOrThrow(long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + id + " не найдена"));
    }
}