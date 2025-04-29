package kz.findmyname284.springbootproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
        @NotBlank String name,
        @NotBlank String surname,
        @NotBlank String username,
        @Email @NotBlank String email,
        @Size(min = 8) String password,
        @NotBlank String address) {
}
