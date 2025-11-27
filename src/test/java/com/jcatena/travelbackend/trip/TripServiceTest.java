package com.jcatena.travelbackend.trip;

import com.jcatena.travelbackend.auth.CurrentUserService;
import com.jcatena.travelbackend.common.exceptions.ForbiddenException;
import com.jcatena.travelbackend.common.exceptions.NotFoundException;
import com.jcatena.travelbackend.expense.Expense;
import com.jcatena.travelbackend.expense.ExpenseRepository;
import com.jcatena.travelbackend.participant.Participant;
import com.jcatena.travelbackend.participant.ParticipantRepository;
import com.jcatena.travelbackend.trip.dto.TripSummaryResponse;
import com.jcatena.travelbackend.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TripService tripService;

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long TRIP_ID = 10L;

    private User owner() {
        return User.builder()
                .id(OWNER_ID)
                .name("Owner")
                .email("owner@example.com")
                .build();
    }

    private Participant participant(Long id, String name, Trip trip) {
        Participant p = Participant.builder()
                .id(id)
                .name(name)
                .trip(trip)
                .build();
        return p;
    }

    private Expense expense(Long id, Trip trip, Participant payer, BigDecimal amount) {
        return Expense.builder()
                .id(id)
                .trip(trip)
                .payer(payer)
                .amount(amount)
                .date(LocalDate.of(2025, 7, 1))
                .description("Test expense " + id)
                .build();
    }

    @Test
    void getSummary_shouldCalculateFairShareAndBalances_correctlyForThreeParticipants() {
        // given
        given(currentUserService.getCurrentUserId()).willReturn(OWNER_ID);

        Trip trip = Trip.builder()
                .id(TRIP_ID)
                .name("Viaje a Asturias")
                .currency("EUR")
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 5))
                .owner(owner())
                .build();

        Participant p1 = participant(101L, "Juan", trip);
        Participant p2 = participant(102L, "María", trip);
        Participant p3 = participant(103L, "Carlos", trip);
        trip.setParticipants(List.of(p1, p2, p3));

        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.of(trip));

        // Gastos: 75 (Juan) + 90 (María) + 125 (Carlos) = 290
        Expense e1 = expense(201L, trip, p1, new BigDecimal("75.00"));
        Expense e2 = expense(202L, trip, p2, new BigDecimal("90.00"));
        Expense e3 = expense(203L, trip, p3, new BigDecimal("125.00"));

        given(expenseRepository.findByTripId(TRIP_ID))
                .willReturn(List.of(e1, e2, e3));

        // when
        TripSummaryResponse summary = tripService.getSummary(TRIP_ID);

        // then
        assertThat(summary.getTripId()).isEqualTo(TRIP_ID);
        assertThat(summary.getTotalAmount()).isEqualByComparingTo("290.00");
        assertThat(summary.getParticipants()).hasSize(3);

        // cada uno debería pagar ~96.67
        // comprobamos balances por nombre
        TripSummaryResponse.ParticipantSummary juan = summary.getParticipants().stream()
                .filter(p -> p.getName().equals("Juan"))
                .findFirst()
                .orElseThrow();

        TripSummaryResponse.ParticipantSummary maria = summary.getParticipants().stream()
                .filter(p -> p.getName().equals("María"))
                .findFirst()
                .orElseThrow();

        TripSummaryResponse.ParticipantSummary carlos = summary.getParticipants().stream()
                .filter(p -> p.getName().equals("Carlos"))
                .findFirst()
                .orElseThrow();

        assertThat(juan.getTotalPaid()).isEqualByComparingTo("75.00");
        assertThat(maria.getTotalPaid()).isEqualByComparingTo("90.00");
        assertThat(carlos.getTotalPaid()).isEqualByComparingTo("125.00");

        // balances: alrededor de -21.67, -6.67, +28.34 (según tu lógica de redondeo + ajuste)
        assertThat(juan.getBalance()).isEqualByComparingTo(new BigDecimal("-21.67"));
        assertThat(maria.getBalance()).isEqualByComparingTo(new BigDecimal("-6.67"));
        assertThat(carlos.getBalance()).isEqualByComparingTo(new BigDecimal("28.34"));

        // suma de balances ≈ 0
        BigDecimal sumBalances = summary.getParticipants().stream()
                .map(TripSummaryResponse.ParticipantSummary::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(sumBalances).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getSummary_shouldThrowForbidden_whenCurrentUserIsNotOwner() {
        // given
        given(currentUserService.getCurrentUserId()).willReturn(OTHER_USER_ID);

        Trip trip = Trip.builder()
                .id(TRIP_ID)
                .name("Viaje a Asturias")
                .owner(owner()) // owner = 1, current user = 2
                .build();

        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.of(trip));

        // when / then
        assertThatThrownBy(() -> tripService.getSummary(TRIP_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("do not own this trip");
    }

    @Test
    void getSummary_shouldThrowNotFound_whenTripDoesNotExist() {
        // given
        given(currentUserService.getCurrentUserId()).willReturn(OWNER_ID);
        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> tripService.getSummary(TRIP_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip not found with id: " + TRIP_ID);
    }

    @Test
    void deleteTrip_shouldDeleteExpensesParticipantsAndTrip_forOwner() {
        // given
        given(currentUserService.getCurrentUserId()).willReturn(OWNER_ID);

        Trip trip = Trip.builder()
                .id(TRIP_ID)
                .name("Viaje a Asturias")
                .owner(owner())
                .build();

        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.of(trip));

        // when
        tripService.deleteTrip(TRIP_ID);

        // then
        verify(expenseRepository).deleteAllByTripId(TRIP_ID);
        verify(participantRepository).deleteAllByTripId(TRIP_ID);
        verify(tripRepository).delete(trip);
    }
}
