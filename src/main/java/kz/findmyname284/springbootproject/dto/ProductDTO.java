package kz.findmyname284.springbootproject.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record ProductDTO(
        @NotBlank(message = "Требуется название продукта") String name,
        String description,
        @DecimalMin(value = "0.01", message = "Цена должна быть больше 0.01") BigDecimal price,
        @NotBlank(message = "Требуется штрих-код") String barcode,
        @NotBlank(message = "Требуется категория") String category,
        @NotBlank(message = "Требуется изображение") String image) {
}