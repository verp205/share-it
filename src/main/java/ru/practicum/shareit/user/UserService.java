package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto dto);

    UserDto updateUser(Long id, UserDto dto);

    UserDto getUser(Long id);

    List<UserDto> getAllUsers();

    void deleteUser(Long id);

    User getUserEntity(Long id);
}
