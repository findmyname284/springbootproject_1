package kz.findmyname284.springbootproject.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record WarehouseProductDTO(
    Long id,
    @NotBlank(message = "штрих-код обязателен") String sku,
    @NotBlank(message = "Требуется количество") Long quantity,
    @NotBlank(message = "Требуется имя") String name,
    @NotBlank(message = "Требуется описание") String description,
    @NotBlank(message = "Требуется изображение") String image,
    @NotBlank(message = "Требуется категория") Long categoryId,
    BigDecimal basePrice,
    Long warehouseId
) {
}