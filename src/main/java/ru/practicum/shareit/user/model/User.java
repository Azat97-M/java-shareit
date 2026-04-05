package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private Long id;
    private String name;
    @Email(message = "Некорректный формат email")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
}
