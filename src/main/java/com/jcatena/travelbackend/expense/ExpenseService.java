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
import org.springframework.security.access.AccessDeniedException;
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

    // ---------- CREATE ----------

    public ExpenseResponse addExpense(Long tripId, Long currentUserId, ExpenseRequest request) {
        // 1) Cargar trip
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));

        // 2) Validar que el usuario es el owner del viaje
        if (!trip.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this trip");
        }

        // 3) Cargar pagador
        Participant payer = participantRepository.findById(request.getPayerId())
                .orElseThrow(() -> new NotFoundException("Participant not found with id: " + request.getPayerId()));

        // 4) Comprobar que el pagador pertenece a ese trip
        if (!payer.getTrip().getId().equals(trip.getId())) {
            throw new IllegalArgumentException("Participant " + payer.getId() + " does not belong to trip " + tripId);
        }

        // 5) Validar fecha dentro del rango del viaje (si ambos existen)
        validateExpenseDateWithinTrip(trip, request.getDate());

        // 6) Construir entidad Expense
        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .trip(trip)
                .payer(payer)
                .build();

        // 7) Guardar
        Expense saved = expenseRepository.save(expense);

        // 8) Mapear a response
        return toResponse(saved);
    }

    // ---------- READ LIST ----------

    public List<ExpenseResponse> getExpensesByTrip(Long tripId, Long currentUserId) {
        // Necesitamos el trip para comprobar owner
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));

        // Validar que el usuario es el owner
        if (!trip.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this trip");
        }

        return expenseRepository.findByTripId(tripId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------- UPDATE ----------

    @Transactional
    public ExpenseResponse updateExpense(Long tripId, Long expenseId, Long currentUserId,
                                         ExpenseUpdateRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with id: " + expenseId));

        // asegurar que el gasto pertenece al trip correcto
        if (!expense.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Expense does not belong to trip " + tripId);
        }

        Trip trip = expense.getTrip();

        // validar que el usuario es el owner del trip
        if (!trip.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this trip");
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
            validateExpenseDateWithinTrip(trip, request.getDate());
            expense.setDate(request.getDate());
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

    // ---------- DELETE ----------

    public void deleteExpense(Long tripId, Long expenseId, Long currentUserId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with id: " + expenseId));

        if (!expense.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Expense does not belong to trip " + tripId);
        }

        Trip trip = expense.getTrip();

        if (!trip.getOwner().getId().equals(currentUserId)) {
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
