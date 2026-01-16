package ru.practicum.shareit.server.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.exception.ConflictException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.model.User;

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

    @Transactional
    @Override
    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь", id));

        if ((dto.getName() == null || dto.getName().isBlank()) &&
                (dto.getEmail() == null || dto.getEmail().isBlank())) {
            throw new ValidationException("Необходимо указать хотя бы одно поле для обновления");
        }

        // Проверка email до сохранения
        if (dto.getEmail() != null && !dto.getEmail().isBlank() &&
                !dto.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email уже используется другим пользователем");
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName().trim());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(dto.getEmail().trim());
        }

        User saved = userRepository.save(user);
        return UserMapper.toUserDto(saved);
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