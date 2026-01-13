package ru.practicum.shareit.server.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.server.exception.ConflictException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(UserServiceImpl.class)
class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ShouldSaveUserInDatabase() {
        UserDto dto = new UserDto("Ivan", "ivan@mail.com");

        UserDto result = userService.createUser(dto);

        assertNotNull(result.getId());

        User savedUser = userRepository.findById(result.getId()).orElseThrow();
        assertEquals("Ivan", savedUser.getName());
        assertEquals("ivan@mail.com", savedUser.getEmail());
    }

    @Test
    void getUser_ShouldReturnUserFromDatabase() {
        User saved = userRepository.save(new User(null, "Anna", "anna@mail.com"));

        UserDto result = userService.getUser(saved.getId());

        assertEquals(saved.getId(), result.getId());
        assertEquals("Anna", result.getName());
        assertEquals("anna@mail.com", result.getEmail());
    }

    @Test
    void getUser_WhenUserNotExists_ShouldThrowNotFound() {
        assertThrows(NotFoundException.class, () ->
                userService.getUser(999L)
        );
    }

    @Test
    void updateUser_ShouldUpdateNameAndEmail() {
        User saved = userRepository.save(new User(null, "Old", "old@mail.com"));

        UserDto updateDto = new UserDto();
        updateDto.setName("New");
        updateDto.setEmail("new@mail.com");

        UserDto result = userService.updateUser(saved.getId(), updateDto);

        assertEquals("New", result.getName());
        assertEquals("new@mail.com", result.getEmail());
    }

    @Test
    void updateUser_WhenNoFieldsProvided_ShouldThrowValidationException() {
        User saved = userRepository.save(new User(null, "User", "user@mail.com"));

        UserDto emptyDto = new UserDto();

        assertThrows(ValidationException.class, () ->
                userService.updateUser(saved.getId(), emptyDto)
        );
    }

    @Test
    void updateUser_WhenEmailAlreadyExists_ShouldThrowConflictException() {
        userRepository.save(new User(null, "User1", "mail1@mail.com"));
        User saved = userRepository.save(new User(null, "User2", "mail2@mail.com"));

        UserDto dto = new UserDto();
        dto.setEmail("mail1@mail.com");

        assertThrows(ConflictException.class, () ->
                userService.updateUser(saved.getId(), dto)
        );
    }

    @Test
    void getAllUsers_ShouldReturnAllUsersFromDatabase() {
        userRepository.save(new User(null, "U1", "u1@mail.com"));
        userRepository.save(new User(null, "U2", "u2@mail.com"));

        List<UserDto> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void deleteUser_ShouldRemoveUserFromDatabase() {
        User saved = userRepository.save(new User(null, "Delete", "delete@mail.com"));

        userService.deleteUser(saved.getId());

        assertFalse(userRepository.existsById(saved.getId()));
    }

    @Test
    void deleteUser_WhenUserNotExists_ShouldThrowNotFound() {
        assertThrows(NotFoundException.class, () ->
                userService.deleteUser(999L)
        );
    }

    @Test
    void getUserEntity_ShouldReturnEntity() {
        User saved = userRepository.save(new User(null, "Entity", "entity@mail.com"));

        User user = userService.getUserEntity(saved.getId());

        assertEquals(saved.getId(), user.getId());
    }
}
