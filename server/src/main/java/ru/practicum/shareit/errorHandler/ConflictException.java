package ru.practicum.shareit.errorHandler;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
