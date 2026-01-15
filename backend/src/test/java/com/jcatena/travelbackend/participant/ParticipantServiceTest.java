package com.jcatena.travelbackend.participant;

import com.jcatena.travelbackend.common.exceptions.NotFoundException;
import com.jcatena.travelbackend.participant.dto.ParticipantRequest;
import com.jcatena.travelbackend.participant.dto.ParticipantResponse;
import com.jcatena.travelbackend.participant.dto.ParticipantUpdateRequest;
import com.jcatena.travelbackend.trip.Trip;
import com.jcatena.travelbackend.trip.TripRepository;
import com.jcatena.travelbackend.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private ParticipantService participantService;

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long TRIP_ID = 10L;
    private static final Long PARTICIPANT_ID = 100L;

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
                .owner(owner())
                .build();
    }

    private Participant participantOfTrip(Trip trip) {
        return Participant.builder()
                .id(PARTICIPANT_ID)
                .name("Juan")
                .trip(trip)
                .build();
    }

    private ParticipantRequest newParticipantRequest() {
        ParticipantRequest req = new ParticipantRequest();
        req.setName("Juan");
        return req;
    }

    @Test
    void addParticipantToTrip_shouldCreateParticipant_forOwner() {
        // given
        Trip trip = tripOwnedByOwner();
        ParticipantRequest request = newParticipantRequest();

        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.of(trip));

        Participant saved = participantOfTrip(trip);
        given(participantRepository.save(any(Participant.class))).willReturn(saved);

        // when
        ParticipantResponse response =
                participantService.addParticipantToTrip(TRIP_ID, OWNER_ID, request);

        // then
        assertThat(response.getId()).isEqualTo(PARTICIPANT_ID);
        assertThat(response.getName()).isEqualTo("Juan");
        assertThat(response.getTripId()).isEqualTo(TRIP_ID);
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void addParticipantToTrip_shouldThrowAccessDenied_whenUserIsNotOwner() {
        // given
        Trip trip = tripOwnedByOwner();
        ParticipantRequest request = newParticipantRequest();

        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.of(trip));

        // when / then
        assertThatThrownBy(() ->
                participantService.addParticipantToTrip(TRIP_ID, OTHER_USER_ID, request)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not the owner of this trip");
    }

    @Test
    void getParticipantsByTrip_shouldThrowNotFound_whenTripDoesNotExist() {
        // given
        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                participantService.getParticipantsByTrip(TRIP_ID, OWNER_ID)
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip not found with id");
    }

    @Test
    void getParticipantsByTrip_shouldThrowAccessDenied_whenUserIsNotOwner() {
        // given
        Trip trip = tripOwnedByOwner();
        given(tripRepository.findById(TRIP_ID)).willReturn(Optional.of(trip));

        // when / then
        assertThatThrownBy(() ->
                participantService.getParticipantsByTrip(TRIP_ID, OTHER_USER_ID)
        )
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateParticipant_shouldUpdateName_forOwnerAndCorrectTrip() {
        // given
        Trip trip = tripOwnedByOwner();
        Participant participant = participantOfTrip(trip);

        ParticipantUpdateRequest request = new ParticipantUpdateRequest();
        request.setName("Carlos");

        given(participantRepository.findById(PARTICIPANT_ID)).willReturn(Optional.of(participant));

        Participant updated = Participant.builder()
                .id(PARTICIPANT_ID)
                .name("Carlos")
                .trip(trip)
                .build();

        given(participantRepository.save(any(Participant.class))).willReturn(updated);

        // when
        ParticipantResponse response =
                participantService.updateParticipant(TRIP_ID, PARTICIPANT_ID, OWNER_ID, request);

        // then
        assertThat(response.getId()).isEqualTo(PARTICIPANT_ID);
        assertThat(response.getName()).isEqualTo("Carlos");
        assertThat(response.getTripId()).isEqualTo(TRIP_ID);
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void updateParticipant_shouldThrowIllegalArgument_whenParticipantDoesNotBelongToTrip() {
        // given
        Trip trip = tripOwnedByOwner();
        Trip otherTrip = Trip.builder().id(999L).build();

        Participant participant = Participant.builder()
                .id(PARTICIPANT_ID)
                .name("Juan")
                .trip(otherTrip)
                .build();

        ParticipantUpdateRequest request = new ParticipantUpdateRequest();
        request.setName("Nuevo nombre");

        given(participantRepository.findById(PARTICIPANT_ID)).willReturn(Optional.of(participant));

        // when / then
        assertThatThrownBy(() ->
                participantService.updateParticipant(TRIP_ID, PARTICIPANT_ID, OWNER_ID, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to trip");
    }

    @Test
    void updateParticipant_shouldThrowAccessDenied_whenUserIsNotOwner() {
        // given
        Trip trip = tripOwnedByOwner();
        Participant participant = participantOfTrip(trip);

        ParticipantUpdateRequest request = new ParticipantUpdateRequest();
        request.setName("Nuevo nombre");

        given(participantRepository.findById(PARTICIPANT_ID)).willReturn(Optional.of(participant));

        // when / then
        assertThatThrownBy(() ->
                participantService.updateParticipant(TRIP_ID, PARTICIPANT_ID, OTHER_USER_ID, request)
        )
                .isInstanceOf(AccessDeniedException.class);
    }
}
