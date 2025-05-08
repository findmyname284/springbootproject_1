package kz.findmyname284.springbootproject.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
                @NotBlank(message = "Имя обязательно") String name,
                @NotBlank(message = "Фамилия обязательна") String surname,
                @NotBlank(message = "Логин обязателен") String username,
                @Email @NotBlank(message = "Email обязателен") String email,
                @NotBlank(message = "Телефон обязателен") String phone,
                @Size(min = 6, message = "Длина пароля должна быть не менее 6 символов") String password,
                @NotBlank(message = "Адрес обязателен") String address,
                String role,
                BigDecimal balance) {
        public RegisterDTO setRole(String role) {
                return new RegisterDTO(name, surname, username, email, phone, password, address, role, balance);
        }

        public RegisterDTO setBalance(BigDecimal balance) {
                return new RegisterDTO(name, surname, username, email, phone, password, address, role, balance);
        }
}
