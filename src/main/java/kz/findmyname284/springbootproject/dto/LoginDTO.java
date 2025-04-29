package kz.findmyname284.springbootproject.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank String username,
        @NotBlank String password) {
}