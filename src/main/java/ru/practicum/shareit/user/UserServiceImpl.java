package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public UserDto createUser(UserDto dto) {
        // Простая проверка email
        if (dto.getEmail() == null || dto.getEmail().isBlank() || !dto.getEmail().contains("@")) {
            throw new IllegalArgumentException("Некорректный email");
        }

        // Проверка уникальности email
        boolean emailExists = storage.values().stream()
                .anyMatch(u -> u.getEmail().equals(dto.getEmail()));

        if (emailExists) {
            throw new IllegalStateException("Пользователь с таким email уже существует");
        }

        User user = UserMapper.toUser(dto);
        user.setId(idGenerator.getAndIncrement());
        storage.put(user.getId(), user);

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto dto) {
        User user = storage.get(id);
        if (user == null) {
            throw new NoSuchElementException("Пользователь с id " + id + " не найден");
        }

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            // Простая проверка email
            if (!dto.getEmail().contains("@")) {
                throw new IllegalArgumentException("Некорректный email");
            }

            // Проверка уникальности email
            boolean emailExists = storage.values().stream()
                    .anyMatch(u -> !u.getId().equals(id) && u.getEmail().equals(dto.getEmail()));

            if (emailExists) {
                throw new IllegalStateException("Email уже используется другим пользователем");
            }

            user.setEmail(dto.getEmail());
        }

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUser(Long id) {
        User user = storage.get(id);
        if (user == null) {
            throw new NoSuchElementException("Пользователь с id " + id + " не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<UserDto> result = new ArrayList<>();
        for (User user : storage.values()) {
            result.add(UserMapper.toUserDto(user));
        }
        return result;
    }

    @Override
    public void deleteUser(Long id) {
        if (!storage.containsKey(id)) {
            throw new NoSuchElementException("Пользователь с id " + id + " не найден");
        }
        storage.remove(id);
    }

    @Override
    public User getUserEntity(Long id) {
        return storage.get(id);
    }
}