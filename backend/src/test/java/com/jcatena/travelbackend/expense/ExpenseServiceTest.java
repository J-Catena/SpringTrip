package com.jcatena.travelbackend.expense;

import com.jcatena.travelbackend.common.exceptions.NotFoundException;
import com.jcatena.travelbackend.expense.dto.ExpenseRequest;
import com.jcatena.travelbackend.expense.dto.ExpenseResponse;
import com.jcatena.travelbackend.participant.Participant;
import com.jcatena.travelbackend.participant.ParticipantRepository;
import com.jcatena.travelbackend.trip.Trip;
import com.jcatena.travelbackend.trip.TripRepository;
import com.jcatena.travelbackend.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long TRIP_ID = 10L;
    private static final Long PAYER_ID = 100L;

    private User owner() {
        return User.builder()
                .id(OWNER_ID)
                .name("Owner")
                .email("owner@example.com")
                .build();
    }

    private Trip tripOwnedByOwner() {
        return Trip.builder()
                .id(TRIP_ID)
                .name("Viaje a Asturias")
                .currency("EUR")
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 5))
                .owner(owner())
                .build();
    }

    private Participant participantOfTrip(Trip trip) {
        return Participant.builder()
                .id(PAYER_ID)
                .name("Juan")
                .trip(trip)
                .build();
    }

    private ExpenseRequest validRequest() {
        ExpenseRequest req = new ExpenseRequest();
        req.setDescription("Cena");
        req.setAmount(new BigDecimal("90.00"));
        req.setDate(LocalDate.of(2025, 7, 2));
        req.setPayerId(PAYER_ID);
        return req;
    }

    @Test
    void addExpense_shouldCreateExpense_forOwnerWithValidData() {
        // given
        Trip trip = tripOwnedByOwner();
        Participant payer = participantOfTrip(trip);
        ExpenseRequest request = validRequest();

        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.of(trip));
        given(participantRepository.findById(PAYER_ID)).willReturn(Optional.of(payer));

        Expense saved = Expense.builder()
                .id(999L)
                .trip(trip)
                .payer(payer)
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .build();

        given(expenseRepository.save(any(Expense.class))).willReturn(saved);

        // when
        ExpenseResponse response = expenseService.addExpense(TRIP_ID, OWNER_ID, request);

        // then
        assertThat(response.getId()).isEqualTo(999L);
        assertThat(response.getTripId()).isEqualTo(TRIP_ID);
        assertThat(response.getPayerId()).isEqualTo(PAYER_ID);
        assertThat(response.getAmount()).isEqualByComparingTo("90.00");
        assertThat(response.getDescription()).isEqualTo("Cena");
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void addExpense_shouldThrowAccessDenied_whenUserIsNotOwner() {
        // given
        Trip trip = tripOwnedByOwner();
        ExpenseRequest request = validRequest();

        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.of(trip));

        // when / then
        assertThatThrownBy(() -> expenseService.addExpense(TRIP_ID, OTHER_USER_ID, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not the owner of this trip");
    }

    @Test
    void addExpense_shouldThrowIllegalArgument_whenPayerDoesNotBelongToTrip() {
        // given
        Trip trip = tripOwnedByOwner();
        Trip otherTrip = Trip.builder().id(999L).build();

        ExpenseRequest request = validRequest();

        Participant payerOtherTrip = Participant.builder()
                .id(PAYER_ID)
                .name("Intruso")
                .trip(otherTrip)
                .build();

        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.of(trip));
        given(participantRepository.findById(PAYER_ID)).willReturn(Optional.of(payerOtherTrip));

        // when / then
        assertThatThrownBy(() -> expenseService.addExpense(TRIP_ID, OWNER_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to trip");
    }

    @Test
    void addExpense_shouldThrowIllegalArgument_whenDateOutsideTripRange() {
        // given
        Trip trip = tripOwnedByOwner();

        ExpenseRequest request = validRequest();
        request.setDate(LocalDate.of(2025, 6, 30)); // antes de startDate

        Participant payer = participantOfTrip(trip);

        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.of(trip));
        given(participantRepository.findById(PAYER_ID)).willReturn(Optional.of(payer));

        // when / then
        assertThatThrownBy(() -> expenseService.addExpense(TRIP_ID, OWNER_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expense date must be within trip dates");
    }

    @Test
    void getExpensesByTrip_shouldThrowNotFound_whenTripDoesNotExist() {
        // given
        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> expenseService.getExpensesByTrip(TRIP_ID, OWNER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip not found with id: " + TRIP_ID);
    }

    @Test
    void getExpensesByTrip_shouldThrowAccessDenied_whenUserIsNotOwner() {
        // given
        Trip trip = tripOwnedByOwner();
        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.of(trip));

        // when / then
        assertThatThrownBy(() -> expenseService.getExpensesByTrip(TRIP_ID, OTHER_USER_ID))
                .isInstanceOf(AccessDeniedException.class);
    }
}
