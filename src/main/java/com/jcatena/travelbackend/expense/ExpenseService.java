package com.jcatena.travelbackend.expense;

import com.jcatena.travelbackend.common.NotFoundException;
import com.jcatena.travelbackend.expense.dto.ExpenseRequest;
import com.jcatena.travelbackend.expense.dto.ExpenseResponse;
import com.jcatena.travelbackend.participant.Participant;
import com.jcatena.travelbackend.participant.ParticipantRepository;
import com.jcatena.travelbackend.trip.Trip;
import com.jcatena.travelbackend.trip.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
            throw new NotFoundException("Participant " + payer.getId() + " does not belong to trip " + tripId);
        }

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

    public List<ExpenseResponse> getExpensesByTrip(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new NotFoundException("Trip not found with id: " + tripId);
        }

        return expenseRepository.findByTripId(tripId)
                .stream()
                .map(this::toResponse)
                .toList();
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
