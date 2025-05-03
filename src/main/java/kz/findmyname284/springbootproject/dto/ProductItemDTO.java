package kz.findmyname284.springbootproject.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductItemDTO(
        @NotBlank Long id,
        @NotBlank Long quantity) {
}