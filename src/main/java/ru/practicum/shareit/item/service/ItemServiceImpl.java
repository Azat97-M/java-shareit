package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.comment.dal.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.errorHandler.NotFoundException;
import ru.practicum.shareit.errorHandler.ValidationException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        LocalDateTime now = LocalDateTime.now();

        boolean hasFinishedBooking = bookingRepository.findByBooker_IdAndItem_IdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, now).isPresent();

        if (!hasFinishedBooking) {
            throw new ValidationException("Вы не можете оставить отзыв: аренда не завершена или не состоялась");
        }

        Comment comment = CommentMapper.toComment(commentDto, item, user);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = getUserOrThrow(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        getUserOrThrow(userId);
        Item existingItem = getItemOrThrow(itemId);

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Вещь с id " + itemId + " не принадлежит пользователю с id " + userId);
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.save(existingItem));
    }

    @Override
    public ItemDto findById(Long itemId, Long userId) {
        Item item = getItemOrThrow(itemId);
        ItemDto itemDto = ItemMapper.toItemDto(item);

        itemDto.setComments(commentRepository.findByItem_Id(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .toList());

        if (item.getOwner().getId().equals(userId)) {
            List<Booking> bookings = bookingRepository.findByItem_IdAndStatusNot(
                    itemId, BookingStatus.REJECTED, Sort.by(Sort.Direction.DESC, "start"));
            itemDto.setLastBooking(calculateLastBooking(bookings));
            itemDto.setNextBooking(calculateNextBooking(bookings));
        }

        return itemDto;
    }

    @Override
    public List<ItemDto> findByOwner(Long userId) {
        getUserOrThrow(userId);
        List<Item> items = itemRepository.findByOwnerId(userId);

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<Booking>> bookingsMap = getBookingsByItemIds(itemIds);
        Map<Long, List<Comment>> commentsMap = getCommentsByItemIds(itemIds);

        return items.stream()
                .map(item -> constructItemDto(item, bookingsMap, commentsMap))
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

    private ItemDto constructItemDto(Item item,
                                     Map<Long, List<Booking>> bookings,
                                     Map<Long, List<Comment>> comments) {

        ItemDto dto = ItemMapper.toItemDto(item);
        Long itemId = item.getId();

        List<Booking> itemBookings = bookings.getOrDefault(itemId, List.of());
        dto.setLastBooking(calculateLastBooking(itemBookings));
        dto.setNextBooking(calculateNextBooking(itemBookings));

        dto.setComments(comments.getOrDefault(itemId, List.of()).stream()
                .map(CommentMapper::toCommentDto)
                .toList());

        return dto;
    }

    private Map<Long, List<Booking>> getBookingsByItemIds(List<Long> itemIds) {
        return bookingRepository.findByItem_IdInAndStatusNot(
                        itemIds, BookingStatus.REJECTED, Sort.by(Sort.Direction.DESC, "start"))
                .stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));
    }

    private Map<Long, List<Comment>> getCommentsByItemIds(List<Long> itemIds) {
        return commentRepository.findByItem_IdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));
    }

    private BookingShortDto calculateLastBooking(List<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(b -> !b.getStart().isAfter(now))
                .findFirst()
                .map(b -> BookingShortDto.builder().id(b.getId()).bookerId(b.getBooker().getId()).build())
                .orElse(null);
    }

    private BookingShortDto calculateNextBooking(List<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .reduce((first, second) -> second)
                .map(b -> BookingShortDto.builder().id(b.getId()).bookerId(b.getBooker().getId()).build())
                .orElse(null);
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