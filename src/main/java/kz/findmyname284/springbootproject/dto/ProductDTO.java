package kz.findmyname284.springbootproject.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record ProductDTO(
        @NotBlank String name,
        String description,
        @DecimalMin("0.01") BigDecimal price,
        @NotBlank String barcode,
        @NotBlank String category,
        @NotBlank String image) {
}