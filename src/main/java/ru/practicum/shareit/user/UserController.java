package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.ValidationGroups;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto createUser(@Validated(ValidationGroups.OnCreate.class) @RequestBody UserDto user) {
        return userService.createUser(user);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable @Positive(message = "ID пользователя должно быть положительным числом") Long id,
                              @Validated(ValidationGroups.OnUpdate.class) @RequestBody UserDto user) {
        return userService.updateUser(id, user);
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable @Positive(message = "ID пользователя должно быть положительным числом") Long id) {
        return userService.getUser(id);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable @Positive(message = "ID пользователя должно быть положительным числом") Long id) {
        userService.deleteUser(id);
    }
}