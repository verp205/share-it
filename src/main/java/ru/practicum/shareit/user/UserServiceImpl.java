package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto createUser(UserDto dto) {
        log.debug("Creating user with email: {}", dto.getEmail());

        User user = UserMapper.toUser(dto);
        User savedUser = userRepository.save(user);

        log.info("User created with id: {}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto dto) {
        log.debug("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь", id));

        User updatedUser = UserMapper.toUser(dto, user);

        try {
            User savedUser = userRepository.save(updatedUser);
            log.info("User with id: {} updated", id);
            return UserMapper.toUserDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email", dto.getEmail(), "уже используется другим пользователем");
        }
    }

    @Override
    public UserDto getUser(Long id) {
        log.debug("Getting user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь", id));

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Getting all users");

        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь", id);
        }

        userRepository.deleteById(id);
        log.info("User with id: {} deleted", id);
    }

    @Override
    public User getUserEntity(Long id) {
        log.debug("Getting user entity with id: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь", id));
    }
}