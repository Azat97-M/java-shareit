package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

public interface BookingService {
    BookingDtoOut create(Long userId, BookingDtoIn bookingDto);

    BookingDtoOut approve(Long userId, Long bookingId, boolean approved);

    BookingDtoOut findById(Long userId, Long bookingId);

    List<BookingDtoOut> findAllByBooker(Long userId, String state);

    List<BookingDtoOut> findAllByOwner(Long userId, String state);
}
