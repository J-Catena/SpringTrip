package com.jcatena.travelbackend.expense.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    // Opcional
    private LocalDate date;

    @NotNull(message = "PayerId is required")
    private Long payerId;
}
