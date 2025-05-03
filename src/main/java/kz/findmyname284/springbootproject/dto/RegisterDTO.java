package kz.findmyname284.springbootproject.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
                @NotBlank String name,
                @NotBlank String surname,
                @NotBlank String username,
                @Email @NotBlank String email,
                @NotBlank String phone,
                @Size(min = 8) String password,
                @NotBlank String address,
                String role,
                BigDecimal balance) {
        public RegisterDTO setRole(String role) {
                return new RegisterDTO(name, surname, username, email, phone, password, address, role, balance);
        }

        public RegisterDTO setBalance(BigDecimal balance) {
                return new RegisterDTO(name, surname, username, email, phone, password, address, role, balance);
        }
}
