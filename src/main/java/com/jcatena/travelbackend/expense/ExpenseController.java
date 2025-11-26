package com.jcatena.travelbackend.expense;

import com.jcatena.travelbackend.common.exceptions.NotFoundException;
import com.jcatena.travelbackend.expense.dto.ExpenseRequest;
import com.jcatena.travelbackend.expense.dto.ExpenseResponse;
import com.jcatena.travelbackend.expense.dto.ExpenseUpdateRequest;
import com.jcatena.travelbackend.user.User;
import com.jcatena.travelbackend.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    private Long getCurrentUserId(UserDetails principal) {
        String email = principal.getUsername();  // lo que usas para logearte
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return user.getId();
    }

    @PostMapping
    public ExpenseResponse addExpense(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ExpenseRequest request
    ) {
        Long userId = getCurrentUserId(principal);
        return expenseService.addExpense(tripId, userId, request);
    }

    @GetMapping
    public List<ExpenseResponse> getExpenses(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = getCurrentUserId(principal);
        return expenseService.getExpensesByTrip(tripId, userId);
    }

    @PutMapping("/{expenseId}")
    public ExpenseResponse updateExpense(
            @PathVariable Long tripId,
            @PathVariable Long expenseId,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ExpenseUpdateRequest request
    ) {
        Long userId = getCurrentUserId(principal);
        return expenseService.updateExpense(tripId, expenseId, userId, request);
    }

    @DeleteMapping("/{expenseId}")
    public void deleteExpense(
            @PathVariable Long tripId,
            @PathVariable Long expenseId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = getCurrentUserId(principal);
        expenseService.deleteExpense(tripId, expenseId, userId);
    }
}
