package kz.findmyname284.springbootproject.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record EmployeeDTO(
    @NotBlank Long userId,
    @NotBlank BigDecimal salary,
    @NotBlank String iin
) {
    
}
