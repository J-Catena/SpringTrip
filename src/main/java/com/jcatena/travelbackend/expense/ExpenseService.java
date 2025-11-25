package com.jcatena.travelbackend.expense;

import com.jcatena.travelbackend.common.exceptions.NotFoundException;
import com.jcatena.travelbackend.expense.dto.ExpenseRequest;
import com.jcatena.travelbackend.expense.dto.ExpenseResponse;
import com.jcatena.travelbackend.expense.dto.ExpenseUpdateRequest;
import com.jcatena.travelbackend.participant.Participant;
import com.jcatena.travelbackend.participant.ParticipantRepository;
import com.jcatena.travelbackend.trip.Trip;
import com.jcatena.travelbackend.trip.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final ParticipantRepository participantRepository;

    public ExpenseResponse addExpense(Long tripId, ExpenseRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));

        Participant payer = participantRepository.findById(request.getPayerId())
                .orElseThrow(() -> new NotFoundException("Participant not found with id: " + request.getPayerId()));

        // comprobación importante: el pagador debe pertenecer al trip
        if (!payer.getTrip().getId().equals(trip.getId())) {
            throw new IllegalArgumentException("Participant " + payer.getId() + " does not belong to trip " + tripId);
        }

        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .trip(trip)
                .payer(payer)
                .build();

        validateExpenseDateWithinTrip(trip, request.getDate());


        Expense saved = expenseRepository.save(expense);

        return toResponse(saved);
    }

    public List<ExpenseResponse> getExpensesByTrip(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new NotFoundException("Trip not found with id: " + tripId);
        }

        return expenseRepository.findByTripId(tripId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ExpenseResponse updateExpense(Long tripId, Long expenseId, ExpenseUpdateRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with id: " + expenseId));

        // asegurar que el gasto pertenece al trip correcto
        if (!expense.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Expense does not belong to trip " + tripId);
        }

        // amount
        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            expense.setAmount(request.getAmount());
        }

        // description
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }

        // date
        if (request.getDate() != null) {
            expense.setDate(request.getDate());
            validateExpenseDateWithinTrip(expense.getTrip(), expense.getDate());
        }


        // payerId (cambiar quién paga el gasto)
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

    private void validateExpenseDateWithinTrip(Trip trip, LocalDate expenseDate) {
        if (expenseDate == null || trip.getStartDate() == null || trip.getEndDate() == null) {
            return;
        }

        if (expenseDate.isBefore(trip.getStartDate()) || expenseDate.isAfter(trip.getEndDate())) {
            throw new IllegalArgumentException("Expense date must be within trip dates");
        }
    }

    public void deleteExpense(Long tripId, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with id: " + expenseId));

        if (!expense.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Expense does not belong to trip " + tripId);
        }

        expenseRepository.delete(expense);
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
