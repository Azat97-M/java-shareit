package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemDto findById(Long itemId, Long userId);

    List<ItemDto> findByOwner(Long userId);

    List<ItemDto> search(String text);
}
