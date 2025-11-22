package com.jcatena.travelbackend.expense;

import com.jcatena.travelbackend.expense.dto.ExpenseRequest;
import com.jcatena.travelbackend.expense.dto.ExpenseResponse;
import com.jcatena.travelbackend.expense.dto.ExpenseUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ExpenseResponse addExpense(@PathVariable Long tripId,
                                      @Valid @RequestBody ExpenseRequest request) {
        return expenseService.addExpense(tripId, request);
    }

    @GetMapping
    public List<ExpenseResponse> getExpenses(@PathVariable Long tripId) {
        return expenseService.getExpensesByTrip(tripId);
    }

    @PutMapping("/{expenseId}")
    public ExpenseResponse updateExpense(@PathVariable Long tripId,
                                         @PathVariable Long expenseId,
                                         @RequestBody ExpenseUpdateRequest request) {
        return expenseService.updateExpense(tripId, expenseId, request);
    }
}
