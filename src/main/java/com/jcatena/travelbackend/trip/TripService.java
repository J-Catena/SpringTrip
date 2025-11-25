package com.jcatena.travelbackend.trip;

import com.jcatena.travelbackend.auth.CurrentUserService;
import com.jcatena.travelbackend.common.exceptions.ForbiddenException;
import com.jcatena.travelbackend.common.exceptions.NotFoundException;
import com.jcatena.travelbackend.expense.Expense;
import com.jcatena.travelbackend.expense.ExpenseRepository;
import com.jcatena.travelbackend.participant.Participant;
import com.jcatena.travelbackend.participant.ParticipantRepository;
import com.jcatena.travelbackend.trip.dto.TripRequest;
import com.jcatena.travelbackend.trip.dto.TripResponse;
import com.jcatena.travelbackend.trip.dto.TripSettlementResponse;
import com.jcatena.travelbackend.trip.dto.TripSummaryResponse;
import com.jcatena.travelbackend.trip.dto.TripUpdateRequest;
import com.jcatena.travelbackend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TripService {

    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;
    private final ParticipantRepository participantRepository;
    private final CurrentUserService currentUserService;

    // ---------- Helpers de seguridad ----------

    private Trip getTripForCurrentUser(Long tripId) {
        Long userId = currentUserService.getCurrentUserId();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));

        if (trip.getOwner() == null || !trip.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this trip");
        }

        return trip;
    }

    // ---------- CRUD principal con seguridad ----------

    // Crear viaje: SIEMPRE para el usuario autenticado
    public TripResponse createTrip(TripRequest request) {

        User owner = currentUserService.getCurrentUser();

        Trip trip = Trip.builder()
                .name(request.getName())
                .description(request.getDescription())
                .currency(request.getCurrency())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .owner(owner)
                .build();

        validateTripDates(trip);

        Trip saved = tripRepository.save(trip);
        return toResponse(saved);
    }

    // Listar viajes del usuario actual
    public List<TripResponse> listTrips() {
        Long userId = currentUserService.getCurrentUserId();

        return tripRepository.findByOwnerId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Obtener viaje concreto del usuario actual
    public TripResponse getTrip(Long id) {
        Trip trip = getTripForCurrentUser(id);
        return toResponse(trip);
    }

    // Modificar datos del viaje del usuario actual
    public TripResponse updateTrip(Long id, TripUpdateRequest request) {
        Trip trip = getTripForCurrentUser(id);

        if (request.getName() != null) {
            trip.setName(request.getName());
        }
        if (request.getDescription() != null) {
            trip.setDescription(request.getDescription());
        }
        if (request.getCurrency() != null) {
            trip.setCurrency(request.getCurrency());
        }
        if (request.getStartDate() != null) {
            trip.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            trip.setEndDate(request.getEndDate());
        }

        validateTripDates(trip);

        Trip saved = tripRepository.save(trip);
        return toResponse(saved);
    }

    // Eliminar viaje (y sus dependencias) del usuario actual
    public void deleteTrip(Long id) {
        Trip trip = getTripForCurrentUser(id);

        Long tripId = trip.getId();

        expenseRepository.deleteAllByTripId(tripId);
        participantRepository.deleteAllByTripId(tripId);
        tripRepository.delete(trip);
    }

    // ---------- Summary + Settlement protegidos ----------

    public TripSummaryResponse getSummary(Long tripId) {

        Trip trip = getTripForCurrentUser(tripId); // aquí ya validas propietario

        // 1. Todos los gastos del viaje
        List<Expense> expenses = expenseRepository.findByTripId(tripId);

        // 2. Total gastado en el viaje
        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Total pagado por cada participante (map id -> cantidad)
        Map<Long, BigDecimal> totalPerParticipant = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getPayer().getId(),
                        Collectors.mapping(
                                Expense::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        // 4. Lista de participantes del viaje
        List<Participant> participantEntities =
                trip.getParticipants() == null ? List.of() : trip.getParticipants();

        int numParticipants = participantEntities.size();

        // 5. Parte justa por persona (fair share)
        BigDecimal fairShare = numParticipants == 0
                ? BigDecimal.ZERO
                : totalAmount.divide(
                BigDecimal.valueOf(numParticipants),
                2,
                RoundingMode.HALF_UP
        );

        // 6. Lista de summaries con balance
        List<TripSummaryResponse.ParticipantSummary> participants =
                participantEntities.stream()
                        .map(p -> {
                            BigDecimal totalPaid =
                                    totalPerParticipant.getOrDefault(p.getId(), BigDecimal.ZERO);
                            BigDecimal balance = totalPaid.subtract(fairShare);

                            return TripSummaryResponse.ParticipantSummary.builder()
                                    .id(p.getId())
                                    .name(p.getName())
                                    .totalPaid(totalPaid)
                                    .balance(balance)
                                    .build();
                        })
                        .collect(Collectors.toList());

        // 7. Ajuste de redondeo
        BigDecimal balanceSum = participants.stream()
                .map(TripSummaryResponse.ParticipantSummary::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (balanceSum.compareTo(BigDecimal.ZERO) != 0 && !participants.isEmpty()) {
            TripSummaryResponse.ParticipantSummary target = participants.stream()
                    .filter(p -> p.getBalance() != null && p.getBalance().compareTo(BigDecimal.ZERO) > 0)
                    .findFirst()
                    .orElse(participants.get(0));

            target.setBalance(target.getBalance().subtract(balanceSum));
        }

        return TripSummaryResponse.builder()
                .tripId(trip.getId())
                .tripName(trip.getName())
                .totalAmount(totalAmount)
                .participants(participants)
                .build();
    }

    public TripSettlementResponse getSettlement(Long tripId) {
        TripSummaryResponse summary = getSummary(tripId); // ya está protegido

        class Side {
            Long id;
            String name;
            BigDecimal remaining;

            Side(Long id, String name, BigDecimal remaining) {
                this.id = id;
                this.name = name;
                this.remaining = remaining;
            }
        }

        List<Side> debtors = new ArrayList<>();
        List<Side> creditors = new ArrayList<>();

        for (TripSummaryResponse.ParticipantSummary p : summary.getParticipants()) {
            BigDecimal balance = p.getBalance();
            if (balance == null || balance.compareTo(BigDecimal.ZERO) == 0) continue;

            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new Side(p.getId(), p.getName(), balance.abs()));
            } else {
                creditors.add(new Side(p.getId(), p.getName(), balance));
            }
        }

        List<TripSettlementResponse.PaymentInstruction> payments = new ArrayList<>();
        int di = 0;
        int ci = 0;

        while (di < debtors.size() && ci < creditors.size()) {
            Side debtor = debtors.get(di);
            Side creditor = creditors.get(ci);

            BigDecimal amount = debtor.remaining.min(creditor.remaining);

            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                payments.add(
                        TripSettlementResponse.PaymentInstruction.builder()
                                .payerId(debtor.id)
                                .payerName(debtor.name)
                                .receiverId(creditor.id)
                                .receiverName(creditor.name)
                                .amount(amount)
                                .build()
                );
            }

            debtor.remaining = debtor.remaining.subtract(amount);
            creditor.remaining = creditor.remaining.subtract(amount);

            if (debtor.remaining.compareTo(BigDecimal.ZERO) == 0) di++;
            if (creditor.remaining.compareTo(BigDecimal.ZERO) == 0) ci++;
        }

        return TripSettlementResponse.builder()
                .tripId(summary.getTripId())
                .tripName(summary.getTripName())
                .payments(payments)
                .build();
    }

    // ---------- Helpers ----------

    private void validateTripDates(Trip trip) {
        if (trip.getStartDate() != null && trip.getEndDate() != null
                && trip.getStartDate().isAfter(trip.getEndDate())) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }
    }

    private TripResponse toResponse(Trip trip) {
        return TripResponse.builder()
                .id(trip.getId())
                .name(trip.getName())
                .description(trip.getDescription())
                .currency(trip.getCurrency())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .ownerId(trip.getOwner() != null ? trip.getOwner().getId() : null)
                .build();
    }
}
