package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public UserDto createUser(UserDto dto) {
        log.debug("Creating user with email: {}", dto.getEmail());

        boolean emailExists = storage.values().stream()
                .anyMatch(u -> u.getEmail().equals(dto.getEmail()));

        if (emailExists) {
            throw new ConflictException("Пользователь", "email", dto.getEmail());
        }

        User user = UserMapper.toUser(dto);
        user.setId(idGenerator.getAndIncrement());
        storage.put(user.getId(), user);

        log.info("User created with id: {}", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto dto) {
        log.debug("Updating user with id: {}", id);

        User user = storage.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь", id);
        }

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            boolean emailExists = storage.values().stream()
                    .anyMatch(u -> !u.getId().equals(id) && u.getEmail().equals(dto.getEmail()));

            if (emailExists) {
                throw new ConflictException("Email", dto.getEmail(), "уже используется другим пользователем");
            }

            user.setEmail(dto.getEmail());
        }

        log.info("User with id: {} updated", id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUser(Long id) {
        log.debug("Getting user with id: {}", id);

        User user = storage.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь", id);
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Getting all users");
        return storage.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);

        if (!storage.containsKey(id)) {
            throw new NotFoundException("Пользователь", id);
        }
        storage.remove(id);
        log.info("User with id: {} deleted", id);
    }

    @Override
    public User getUserEntity(Long id) {
        log.debug("Getting user entity with id: {}", id);

        User user = storage.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь", id);
        }
        return user;
    }
}