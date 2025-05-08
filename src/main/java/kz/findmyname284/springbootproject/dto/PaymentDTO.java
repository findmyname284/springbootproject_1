package kz.findmyname284.springbootproject.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentDTO(
        @NotBlank(message = "Требуется идентификатор") Long id,
        @NotBlank(message = "Имя обязательно") String name,
        @NotBlank(message = "Адрес обязателен") String address,
        @NotBlank(message = "Требуется номер карты") String cardNumber,
        @NotBlank(message = "Требуется указать дату истечения срока действия.") String expiryDate,
        @NotBlank(message = "Требуется CVV-код карты") String cardCvv) {

}
