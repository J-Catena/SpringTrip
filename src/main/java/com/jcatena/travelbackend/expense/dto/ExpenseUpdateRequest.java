package com.jcatena.travelbackend.expense.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpenseUpdateRequest {

    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    private LocalDate date;

    private Long payerId;
}
