package com.jcatena.travelbackend.expense.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ExpenseUpdateRequest {

    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private Long payerId;
}
