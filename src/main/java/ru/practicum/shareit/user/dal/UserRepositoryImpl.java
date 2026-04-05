package ru.practicum.shareit.user.dal;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.errorHandler.ConflictException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        checkEmailUniqueness(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(Long userId, User user) {
        User existing = users.get(userId);
        if (user.getEmail() != null) {
            checkEmailUniqueness(user);
            existing.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            existing.setName(user.getName());
        }
        return existing;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void checkEmailUniqueness(User user) {
        boolean emailExists = users.values().stream()
                .anyMatch(user1 -> user1.getEmail().equalsIgnoreCase(user.getEmail()));

        if (emailExists) {
            throw new ConflictException("Email " + user.getEmail() + " уже занят");
        }
    }
}
