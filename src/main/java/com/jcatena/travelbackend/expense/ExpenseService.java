package com.jcatena.travelbackend.expense;

import com.jcatena.travelbackend.auth.CurrentUserService;
import com.jcatena.travelbackend.common.exceptions.NotFoundException;
import com.jcatena.travelbackend.expense.dto.ExpenseRequest;
import com.jcatena.travelbackend.expense.dto.ExpenseResponse;
import com.jcatena.travelbackend.expense.dto.ExpenseUpdateRequest;
import com.jcatena.travelbackend.participant.Participant;
import com.jcatena.travelbackend.participant.ParticipantRepository;
import com.jcatena.travelbackend.trip.Trip;
import com.jcatena.travelbackend.trip.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final ParticipantRepository participantRepository;
    private final CurrentUserService currentUserService;

    // ---------- CREATE ----------

    public ExpenseResponse addExpense(Long tripId, ExpenseRequest request) {
        Long currentUserId = currentUserService.getCurrentUserId();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));

        if (trip.getOwner() == null || !trip.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this trip");
        }

        Participant payer = participantRepository.findById(request.getPayerId())
                .orElseThrow(() -> new NotFoundException("Participant not found with id: " + request.getPayerId()));

        if (!payer.getTrip().getId().equals(trip.getId())) {
            throw new IllegalArgumentException(
                    "Participant " + payer.getId() + " does not belong to trip " + tripId
            );
        }

        validateExpenseDateWithinTrip(trip, request.getDate());

        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .trip(trip)
                .payer(payer)
                .build();

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    // ---------- READ LIST ----------

    public List<ExpenseResponse> getExpensesByTrip(Long tripId) {
        Long currentUserId = currentUserService.getCurrentUserId();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));

        if (trip.getOwner() == null || !trip.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this trip");
        }

        return expenseRepository.findByTripId(tripId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------- UPDATE ----------

    public ExpenseResponse updateExpense(Long tripId, Long expenseId, ExpenseUpdateRequest request) {
        Long currentUserId = currentUserService.getCurrentUserId();

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with id: " + expenseId));

        if (!expense.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Expense does not belong to trip " + tripId);
        }

        Trip trip = expense.getTrip();

        if (trip.getOwner() == null || !trip.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this trip");
        }

        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            expense.setAmount(request.getAmount());
        }

        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }

        if (request.getDate() != null) {
            validateExpenseDateWithinTrip(trip, request.getDate());
            expense.setDate(request.getDate());
        }

        if (request.getPayerId() != null) {
            Participant newPayer = participantRepository.findById(request.getPayerId())
                    .orElseThrow(() -> new NotFoundException("Participant not found with id: " + request.getPayerId()));

            if (!newPayer.getTrip().getId().equals(tripId)) {
                throw new IllegalArgumentException("Payer does not belong to trip " + tripId);
            }

            expense.setPayer(newPayer);
        }

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    // ---------- DELETE ----------

    public void deleteExpense(Long tripId, Long expenseId) {
        Long currentUserId = currentUserService.getCurrentUserId();

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with id: " + expenseId));

        if (!expense.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Expense does not belong to trip " + tripId);
        }

        Trip trip = expense.getTrip();

        if (trip.getOwner() == null || !trip.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this trip");
        }

        expenseRepository.delete(expense);
    }

    // ---------- HELPERS ----------

    private void validateExpenseDateWithinTrip(Trip trip, LocalDate expenseDate) {
        if (expenseDate == null || trip.getStartDate() == null || trip.getEndDate() == null) {
            return;
        }

        if (expenseDate.isBefore(trip.getStartDate()) || expenseDate.isAfter(trip.getEndDate())) {
            throw new IllegalArgumentException("Expense date must be within trip dates");
        }
    }

    private ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .date(expense.getDate())
                .tripId(expense.getTrip().getId())
                .payerId(expense.getPayer().getId())
                .payerName(expense.getPayer().getName())
                .build();
    }
}
