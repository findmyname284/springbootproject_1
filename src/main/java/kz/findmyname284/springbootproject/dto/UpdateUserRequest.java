package kz.findmyname284.springbootproject.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
                @NotBlank(message = "Имя обязательно") String name,
                @NotBlank(message = "Фамилия обязательна") String surname,
                @NotBlank(message = "Логин обязателен")
                @Size(min = 3, max = 20, message = "Длина логина от 3 до 20 символов") String username,
                @NotBlank(message = "Телефон обязателен") String phone,
                @NotBlank(message = "Email обязателен")
                @Email(message = "Email имеет неправильный формат") String email,
                @NotBlank(message = "Адрес обязателен") String address,
                @PositiveOrZero(message = "Баланс не может быть отрицательным") BigDecimal balance,
                @NotNull(message = "Роль обязательна") String role) {
}
