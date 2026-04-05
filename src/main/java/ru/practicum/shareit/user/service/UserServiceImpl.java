package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errorHandler.NotFoundException;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.create(user));
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        getUserOrThrow(userId);
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.update(userId, user));
    }

    @Override
    public UserDto findById(Long userId) {
        return UserMapper.toUserDto(getUserOrThrow(userId));
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long userId) {
        getUserOrThrow(userId);
        userRepository.delete(userId);
    }

    private User getUserOrThrow(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }
}