package ru.practicum.shareit.user.dal;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User create(User user);

    User update(Long userId, User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    void delete(Long userId);
}
