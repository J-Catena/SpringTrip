package com.jcatena.travelbackend.trip;

import com.jcatena.travelbackend.common.NotFoundException;
import com.jcatena.travelbackend.expense.Expense;
import com.jcatena.travelbackend.expense.ExpenseRepository;
import com.jcatena.travelbackend.participant.Participant;
import com.jcatena.travelbackend.trip.dto.TripRequest;
import com.jcatena.travelbackend.trip.dto.TripResponse;
import com.jcatena.travelbackend.trip.dto.TripSummaryResponse;
import com.jcatena.travelbackend.trip.dto.TripSettlementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;

    // Crear viaje
    public TripResponse createTrip(TripRequest request) {
        Trip trip = Trip.builder()
                .name(request.getName())
                .description(request.getDescription())
                .currency(request.getCurrency())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Trip saved = tripRepository.save(trip);
        return toResponse(saved);
    }

    // Listar todos los viajes
    public List<TripResponse> getAllTrips() {
        return tripRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Obtener viaje por Id
    public TripResponse getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + id));
        return toResponse(trip);
    }

    // Summary del viaje
    public TripSummaryResponse getSummary(Long tripId) {

        // 1. Cargamos el viaje
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));

        // 2. Todos los gastos del viaje
        List<Expense> expenses = expenseRepository.findByTripId(tripId);

        // 3. Total gastado en el viaje
        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Total pagado por cada participante (map id -> importe)
        Map<Long, BigDecimal> totalPerParticipant = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getPayer().getId(),
                        Collectors.mapping(
                                Expense::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        // 5. Lista de participantes del viaje
        List<Participant> participantEntities =
                trip.getParticipants() == null ? List.of() : trip.getParticipants();

        int numParticipants = participantEntities.size();

        // 6. Parte justa por persona (fair share)
        BigDecimal fairShare = numParticipants == 0
                ? BigDecimal.ZERO
                : totalAmount.divide(
                BigDecimal.valueOf(numParticipants),
                2,
                RoundingMode.HALF_UP
        );

        // 7. Construimos el resumen por participante (incluye balance)
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
                        .toList();

        // 8. Devolvemos el summary completo del viaje
        return TripSummaryResponse.builder()
                .tripId(trip.getId())
                .tripName(trip.getName())
                .totalAmount(totalAmount)
                .participants(participants)
                .build();
    }

    public TripSettlementResponse getSettlement(Long tripId) {
        // 1. Reutilizamos el summary que ya funciona
        TripSummaryResponse summary = getSummary(tripId);

        // 2. Preparamos estructuras internas para deudores y acreedores
        class Side {
            Long id;
            String name;
            BigDecimal remaining; // siempre positivo

            Side(Long id, String name, BigDecimal remaining) {
                this.id = id;
                this.name = name;
                this.remaining = remaining;
            }
        }

        List<Side> debtors = new ArrayList<>();
        List<Side> creditors = new ArrayList<>();

        // 3. Separamos participantes según su balance
        for (TripSummaryResponse.ParticipantSummary p : summary.getParticipants()) {
            BigDecimal balance = p.getBalance();
            if (balance == null || balance.compareTo(BigDecimal.ZERO) == 0) {
                continue; // ni debe ni le deben
            }

            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                // deudor: guardamos la cantidad en positivo
                debtors.add(new Side(
                        p.getId(),
                        p.getName(),
                        balance.abs() // |-negativo|
                ));
            } else {
                // acreedor: balance ya es cantidad positiva a recibir
                creditors.add(new Side(
                        p.getId(),
                        p.getName(),
                        balance
                ));
            }
        }

        // 4. Emparejamos deudores y acreedores
        List<TripSettlementResponse.PaymentInstruction> payments = new ArrayList<>();

        int di = 0; // índice deudores
        int ci = 0; // índice acreedores

        while (di < debtors.size() && ci < creditors.size()) {
            Side debtor = debtors.get(di);
            Side creditor = creditors.get(ci);

            // cantidad a pagar en este paso: el mínimo entre lo que debe y lo que le deben
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

            // restamos el pago a ambos
            debtor.remaining = debtor.remaining.subtract(amount);
            creditor.remaining = creditor.remaining.subtract(amount);

            // si el deudor ya ha saldado su deuda, pasamos al siguiente
            if (debtor.remaining.compareTo(BigDecimal.ZERO) == 0) {
                di++;
            }

            // si el acreedor ya ha cobrado todo lo suyo, pasamos al siguiente
            if (creditor.remaining.compareTo(BigDecimal.ZERO) == 0) {
                ci++;
            }
        }

        // 5. Devolvemos la respuesta completa
        return TripSettlementResponse.builder()
                .tripId(summary.getTripId())
                .tripName(summary.getTripName())
                .payments(payments)
                .build();
    }



    // Conversor entidad -> DTO de viaje
    private TripResponse toResponse(Trip trip) {
        return TripResponse.builder()
                .id(trip.getId())
                .name(trip.getName())
                .description(trip.getDescription())
                .currency(trip.getCurrency())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .build();
    }
}
