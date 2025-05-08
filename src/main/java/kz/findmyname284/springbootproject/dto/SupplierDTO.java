package kz.findmyname284.springbootproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SupplierDTO(
        @NotBlank(message = "Имя обязательно") String name,
        @NotBlank(message = "Телефон обязателен") String phone,
        @Email @NotBlank(message = "Email обязателен") String email,
        @NotBlank(message = "Адрес обязателен") String address) {
}
