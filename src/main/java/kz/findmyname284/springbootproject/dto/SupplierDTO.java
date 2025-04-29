package kz.findmyname284.springbootproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SupplierDTO(
        @NotBlank String name,
        @NotBlank String phone,
        @Email @NotBlank String email,
        @NotBlank String address) {
}
