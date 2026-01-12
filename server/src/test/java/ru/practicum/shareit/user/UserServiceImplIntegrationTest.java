package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.ShareItServerApplication;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ContextConfiguration(classes = ShareItServerApplication.class)
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_withValidData_shouldCreateUser() {
        // Given
        UserDto userDto = new UserDto(null, "John Doe", "john.doe@example.com");

        // When
        UserDto createdUser = userService.createUser(userDto);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("John Doe");
        assertThat(createdUser.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void updateUser_whenUserExists_shouldUpdateUser() {
        // Given - создаем пользователя
        UserDto userDto = new UserDto(null, "Original Name", "original@example.com");
        UserDto createdUser = userService.createUser(userDto);
        Long userId = createdUser.getId();

        // Given - данные для обновления
        UserDto updateDto = new UserDto(null, "Updated Name", "updated@example.com");

        // When
        UserDto updatedUser = userService.updateUser(userId, updateDto);

        // Then
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(userId);
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void updateUser_whenUserNotFound_shouldThrowNotFoundException() {
        // Given
        Long nonExistentUserId = 999L;
        UserDto updateDto = new UserDto(null, "Name", "email@example.com");

        // When & Then
        assertThrows(NotFoundException.class,
                () -> userService.updateUser(nonExistentUserId, updateDto));
    }

    @Test
    void updateUser_whenUpdateOnlyName_shouldKeepEmail() {
        // Given
        UserDto userDto = new UserDto(null, "Original Name", "keep.email@example.com");
        UserDto createdUser = userService.createUser(userDto);
        Long userId = createdUser.getId();

        // Обновляем только имя
        UserDto updateDto = new UserDto(null, "New Name", null);

        // When
        UserDto updatedUser = userService.updateUser(userId, updateDto);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("New Name");
        assertThat(updatedUser.getEmail()).isEqualTo("keep.email@example.com"); // Email остался прежним
    }

    @Test
    void updateUser_whenUpdateOnlyEmail_shouldKeepName() {
        // Given
        UserDto userDto = new UserDto(null, "Keep Name", "old.email@example.com");
        UserDto createdUser = userService.createUser(userDto);
        Long userId = createdUser.getId();

        // Обновляем только email
        UserDto updateDto = new UserDto(null, null, "new.email@example.com");

        // When
        UserDto updatedUser = userService.updateUser(userId, updateDto);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("Keep Name"); // Имя осталось прежним
        assertThat(updatedUser.getEmail()).isEqualTo("new.email@example.com");
    }

    @Test
    void getUser_whenUserExists_shouldReturnUser() {
        // Given
        UserDto userDto = new UserDto(null, "Test User", "test@example.com");
        UserDto createdUser = userService.createUser(userDto);
        Long userId = createdUser.getId();

        // When
        UserDto retrievedUser = userService.getUser(userId);

        // Then
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getId()).isEqualTo(userId);
        assertThat(retrievedUser.getName()).isEqualTo("Test User");
        assertThat(retrievedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getUser_whenUserNotFound_shouldThrowNotFoundException() {
        // Given
        Long nonExistentUserId = 999L;

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.getUser(nonExistentUserId));
    }

    @Test
    void getAllUsers_whenUsersExist_shouldReturnList() {
        // Given - создаем несколько пользователей
        UserDto user1 = new UserDto(null, "User 1", "user1@example.com");
        UserDto user2 = new UserDto(null, "User 2", "user2@example.com");
        UserDto user3 = new UserDto(null, "User 3", "user3@example.com");

        userService.createUser(user1);
        userService.createUser(user2);
        userService.createUser(user3);

        // When
        List<UserDto> allUsers = userService.getAllUsers();

        // Then
        assertThat(allUsers).hasSizeGreaterThanOrEqualTo(3);

        // Проверяем, что наши пользователи есть в списке
        boolean hasUser1 = allUsers.stream()
                .anyMatch(u -> "user1@example.com".equals(u.getEmail()));
        boolean hasUser2 = allUsers.stream()
                .anyMatch(u -> "user2@example.com".equals(u.getEmail()));
        boolean hasUser3 = allUsers.stream()
                .anyMatch(u -> "user3@example.com".equals(u.getEmail()));

        assertThat(hasUser1).isTrue();
        assertThat(hasUser2).isTrue();
        assertThat(hasUser3).isTrue();
    }

    @Test
    void getAllUsers_whenNoUsers_shouldReturnEmptyList() {
        // When - база должна быть пуста из-за @Transactional
        List<UserDto> allUsers = userService.getAllUsers();

        // Then
        assertThat(allUsers).isEmpty();
    }

    @Test
    void deleteUser_whenUserExists_shouldDeleteUser() {
        // Given - создаем пользователя
        UserDto userDto = new UserDto(null, "To Delete", "delete@example.com");
        UserDto createdUser = userService.createUser(userDto);
        Long userId = createdUser.getId();

        // Проверяем, что пользователь создан
        assertThat(userService.getUser(userId)).isNotNull();

        // When
        userService.deleteUser(userId);

        // Then - пользователь должен быть удален
        assertThrows(NotFoundException.class, () -> userService.getUser(userId));
    }

    @Test
    void deleteUser_whenUserNotFound_shouldThrowNotFoundException() {
        // Given
        Long nonExistentUserId = 999L;

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.deleteUser(nonExistentUserId));
    }

    @Test
    void getUserEntity_whenUserExists_shouldReturnUserEntity() {
        // Given
        UserDto userDto = new UserDto(null, "Entity User", "entity@example.com");
        UserDto createdUser = userService.createUser(userDto);
        Long userId = createdUser.getId();

        // When
        ru.practicum.shareit.server.user.model.User userEntity = userService.getUserEntity(userId);

        // Then
        assertThat(userEntity).isNotNull();
        assertThat(userEntity.getId()).isEqualTo(userId);
        assertThat(userEntity.getName()).isEqualTo("Entity User");
        assertThat(userEntity.getEmail()).isEqualTo("entity@example.com");
    }

    @Test
    void getUserEntity_whenUserNotFound_shouldThrowNotFoundException() {
        // Given
        Long nonExistentUserId = 999L;

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.getUserEntity(nonExistentUserId));
    }

    @Test
    void updateUser_withSameEmailButDifferentCase_shouldWork() {
        // Given
        String originalEmail = "test@example.com";
        UserDto userDto = new UserDto(null, "User", originalEmail);
        UserDto createdUser = userService.createUser(userDto);
        Long userId = createdUser.getId();

        // Пытаемся обновить с тем же email в верхнем регистре
        UserDto updateDto = new UserDto(null, "Updated User", "TEST@EXAMPLE.COM");

        // When
        UserDto updatedUser = userService.updateUser(userId, updateDto);

        // Then - должно сработать, так как email совпадает (не учитывая регистр в логике)
        assertThat(updatedUser.getEmail()).isEqualTo("TEST@EXAMPLE.COM");
    }

    @Test
    void createUser_withDifferentUsers_shouldHaveDifferentIds() {
        // Given
        UserDto user1Dto = new UserDto(null, "User 1", "user1@example.com");
        UserDto user2Dto = new UserDto(null, "User 2", "user2@example.com");
        UserDto user3Dto = new UserDto(null, "User 3", "user3@example.com");

        // When
        UserDto createdUser1 = userService.createUser(user1Dto);
        UserDto createdUser2 = userService.createUser(user2Dto);
        UserDto createdUser3 = userService.createUser(user3Dto);

        // Then
        assertThat(createdUser1.getId()).isNotNull();
        assertThat(createdUser2.getId()).isNotNull();
        assertThat(createdUser3.getId()).isNotNull();

        // Все ID должны быть разными
        assertThat(createdUser1.getId()).isNotEqualTo(createdUser2.getId());
        assertThat(createdUser1.getId()).isNotEqualTo(createdUser3.getId());
        assertThat(createdUser2.getId()).isNotEqualTo(createdUser3.getId());
    }
}