package kz.findmyname284.springbootproject.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record WarehouseProductDTO(
    Long id,
    @NotBlank String sku,
    @NotBlank Long quantity,
    @NotBlank String name,
    @NotBlank String description,
    @NotBlank String image,
    BigDecimal basePrice,
    Long warehouseId
) {
}