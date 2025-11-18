package com.jcatena.travelbackend.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private Long tripId;
    private Long payerId;
    private String payerName;
}
