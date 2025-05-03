package kz.findmyname284.springbootproject.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentDTO(
        @NotBlank Long id,
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String cardNumber,
        @NotBlank String expiryDate,
        @NotBlank String cardCvv) {

}
