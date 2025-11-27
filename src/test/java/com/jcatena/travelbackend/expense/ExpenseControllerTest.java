package com.jcatena.travelbackend.expense;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcatena.travelbackend.expense.dto.ExpenseRequest;
import com.jcatena.travelbackend.expense.dto.ExpenseResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ExpenseController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@Disabled("Temporarily disabled: security + MockMvc config pending")
class ExpenseControllerTest  {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @Test
    void addExpense_shouldReturnExpenseResponse() throws Exception {
        Long tripId = 1L;

        ExpenseRequest request = new ExpenseRequest();
        request.setAmount(new BigDecimal("100.50"));
        request.setDescription("Hotel");
        request.setDate(LocalDate.of(2025, 1, 10));
        request.setPayerId(10L);

        ExpenseResponse response = ExpenseResponse.builder()
                .id(1L)
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .tripId(tripId)
                .payerId(request.getPayerId())
                .payerName("Juan")
                .build();

        // NOTA: el userId lo dejamos como anyLong() porque el principal será null
        BDDMockito.given(
                expenseService.addExpense(
                        BDDMockito.eq(tripId),
                        anyLong(),
                        any(ExpenseRequest.class)
                )
        ).willReturn(response);

        mockMvc.perform(
                        post("/api/trips/{tripId}/expenses", tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tripId").value(tripId))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.description").value("Hotel"))
                .andExpect(jsonPath("$.payerId").value(10L))
                .andExpect(jsonPath("$.payerName").value("Juan"));
    }

    @Test
    void addExpense_shouldReturnBadRequest_whenAmountInvalid() throws Exception {
        Long tripId = 1L;

        ExpenseRequest request = new ExpenseRequest();
        request.setAmount(new BigDecimal("-5.00")); // inválido por @DecimalMin
        request.setDescription("Bad expense");
        request.setDate(LocalDate.of(2025, 1, 10));
        request.setPayerId(10L);

        mockMvc.perform(
                        post("/api/trips/{tripId}/expenses", tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                // mensaje EXACTO de tu @DecimalMin
                .andExpect(jsonPath("$.errors.amount")
                        .value("Amount must be greater than 0"));
    }
}
