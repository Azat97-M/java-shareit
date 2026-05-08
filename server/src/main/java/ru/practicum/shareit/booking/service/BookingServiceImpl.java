package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.errorHandler.NotFoundException;
import ru.practicum.shareit.errorHandler.ValidationException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private final Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingDtoOut create(Long userId, BookingDtoIn bookingDto) {
        User user = getUserOrThrow(userId);
        Item item = getItemOrThrow(bookingDto.getItemId());

        validateBookingDates(bookingDto);

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь с id " + item.getId() + " недоступна");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toBookingDtoOut(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDtoOut approve(Long userId, Long bookingId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Пользователь " + userId + " не является владельцем вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования уже изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDtoOut(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoOut findById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();

        if (!userId.equals(bookerId) && !userId.equals(ownerId)) {
            throw new NotFoundException("Доступ к бронированию запрещен для пользователя " + userId);
        }
        return BookingMapper.toBookingDtoOut(booking);
    }

    @Override
    public List<BookingDtoOut> findAllByBooker(Long userId, String stateStr, Integer from, Integer size) {
        getUserOrThrow(userId);
        BookingState state = parseState(stateStr);
        LocalDateTime now = LocalDateTime.now();

        Pageable pageable = PageRequest.of(from / size, size, sortByStartDesc);

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBooker_Id(userId, pageable);
            case CURRENT -> bookingRepository.findByBooker_IdAndStartIsBeforeAndEndIsAfter(userId, now, now, pageable);
            case PAST -> bookingRepository.findByBooker_IdAndEndIsBefore(userId, now, pageable);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartIsAfter(userId, now, pageable);
            case WAITING -> bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.REJECTED, pageable);
        };

        return bookings.stream().map(BookingMapper::toBookingDtoOut).toList();
    }

    @Override
    public List<BookingDtoOut> findAllByOwner(Long userId, String stateStr, Integer from, Integer size) {
        getUserOrThrow(userId);
        BookingState state = parseState(stateStr);
        LocalDateTime now = LocalDateTime.now();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(from / size, size, sortByStartDesc);

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItem_Owner_Id(userId, pageable);
            case CURRENT ->
                    bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(userId, now, now, pageable);
            case PAST -> bookingRepository.findByItem_Owner_IdAndEndIsBefore(userId, now, pageable);
            case FUTURE -> bookingRepository.findByItem_Owner_IdAndStartIsAfter(userId, now, pageable);
            case WAITING -> bookingRepository.findByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED, pageable);
        };

        return bookings.stream().map(BookingMapper::toBookingDtoOut).toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));
    }

    private void validateBookingDates(BookingDtoIn dto) {
        if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().isEqual(dto.getStart())) {
            throw new ValidationException("Дата окончания не может быть раньше или равна дате начала");
        }
    }

    private BookingState parseState(String state) {
        return BookingState.from(state)
                .orElseThrow(() -> new ValidationException("Неизвестный статус: " + state));
    }
}
