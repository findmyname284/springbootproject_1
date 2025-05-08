package kz.findmyname284.springbootproject.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "Имя пользователя обязательно") String username,
        @NotBlank(message = "Требуется пароль") String password) {
}
