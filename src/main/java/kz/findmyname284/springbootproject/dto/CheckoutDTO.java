package kz.findmyname284.springbootproject.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record CheckoutDTO(
                @NotEmpty(message = "Список товаров не может быть пустым") List<ProductItemDTO> products) {

}