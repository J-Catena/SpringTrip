package com.jcatena.travelbackend.participant;

import com.jcatena.travelbackend.common.NotFoundException;
import com.jcatena.travelbackend.participant.dto.ParticipantRequest;
import com.jcatena.travelbackend.participant.dto.ParticipantResponse;
import com.jcatena.travelbackend.participant.Participant;
import com.jcatena.travelbackend.participant.ParticipantRepository;
import com.jcatena.travelbackend.participant.dto.ParticipantUpdateRequest;
import com.jcatena.travelbackend.common.NotFoundException;
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

    public ParticipantResponse updateParticipant(Long tripId, Long participantId, ParticipantUpdateRequest request) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new NotFoundException("Participant not found with id: " + participantId));

        // validar que pertenece a ese trip
        if (!participant.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Participant does not belong to trip " + tripId);
        }

        if (request.getName() != null) {
            participant.setName(request.getName());
        }

        if (request.getEmail() != null) {
            participant.setEmail(request.getEmail());
        }

        Participant saved = participantRepository.save(participant);
        return toResponse(saved); // igual que con Trip: un mapper privado
    }
}