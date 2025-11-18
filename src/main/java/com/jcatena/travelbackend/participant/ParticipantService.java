package com.jcatena.travelbackend.participant;

import com.jcatena.travelbackend.common.NotFoundException;
import com.jcatena.travelbackend.participant.dto.ParticipantRequest;
import com.jcatena.travelbackend.participant.dto.ParticipantResponse;
import com.jcatena.travelbackend.trip.Trip;
import com.jcatena.travelbackend.trip.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final TripRepository tripRepository;

    public ParticipantResponse addParticipantToTrip(Long tripId, ParticipantRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));

        Participant participant = Participant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .trip(trip)
                .build();

        Participant saved = participantRepository.save(participant);

        return toResponse(saved);
    }

    public List<ParticipantResponse> getParticipantsByTrip(Long tripId) {
        // aseguramos que el viaje existe
        if (!tripRepository.existsById(tripId)) {
            throw new NotFoundException("Trip not found with id: " + tripId);
        }

        return participantRepository.findByTripId(tripId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ParticipantResponse toResponse(Participant participant) {
        return ParticipantResponse.builder()
                .id(participant.getId())
                .name(participant.getName())
                .email(participant.getEmail())
                .tripId(participant.getTrip().getId())
                .build();
    }
}
