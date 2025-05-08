package kz.findmyname284.springbootproject.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductItemDTO(
        @NotBlank(message = "Требуется идентификатор") Long id,
        @NotBlank(message = "Требуется количество") Long quantity) {
}