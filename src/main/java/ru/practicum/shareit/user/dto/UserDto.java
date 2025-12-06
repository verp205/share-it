package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;

    // Конструктор без ID для создания
    public UserDto(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
