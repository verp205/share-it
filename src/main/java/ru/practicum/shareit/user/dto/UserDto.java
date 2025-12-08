package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.ValidationGroups;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @Null(groups = ValidationGroups.OnCreate.class, message = "ID должен быть null при создании")
    private Long id;

    @NotBlank(groups = ValidationGroups.OnCreate.class, message = "Имя не может быть пустым")
    private String name;

    @NotBlank(groups = ValidationGroups.OnCreate.class, message = "Email не может быть пустым")
    @Email(groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class},
            message = "Некорректный формат email")
    private String email;

    public UserDto(String name, String email) {
        this.name = name;
        this.email = email;
    }
}