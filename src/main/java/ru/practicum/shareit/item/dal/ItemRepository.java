package ru.practicum.shareit.item.dal;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item create(Long userId, Item item);

    Item update(Long userId, Long itemId, Item item);

    Optional<Item> findById(Long itemId);

    List<Item> findByOwnerId(Long ownerId);

    List<Item> search(String text);
}
