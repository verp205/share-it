package ru.practicum.shareit.server.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(UserServiceImpl.class)
class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_ShouldSaveUserInDatabase() {
        UserDto dto = new UserDto("Ivan", "ivan@mail.com");

        UserDto result = userService.createUser(dto);

        assertNotNull(result.getId());

        User savedUser = userRepository.findById(result.getId()).orElseThrow();
        assertEquals("Ivan", savedUser.getName());
        assertEquals("ivan@mail.com", savedUser.getEmail());
    }
}