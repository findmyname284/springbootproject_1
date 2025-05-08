package kz.findmyname284.springbootproject.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record EmployeeDTO(
    @NotBlank(message = "Требуется идентификатор пользователя") Long userId,
    @NotBlank(message = "Требуется зарплата") BigDecimal salary,
    @NotBlank(message = "ИИН требуется") String iin
) {
    
}
