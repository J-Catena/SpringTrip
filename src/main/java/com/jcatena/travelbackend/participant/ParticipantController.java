package com.jcatena.travelbackend.participant;

import com.jcatena.travelbackend.participant.dto.ParticipantRequest;
import com.jcatena.travelbackend.participant.dto.ParticipantResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @PostMapping
    public ParticipantResponse addParticipant(@PathVariable Long tripId,
                                              @Valid @RequestBody ParticipantRequest request) {
        return participantService.addParticipantToTrip(tripId, request);
    }

    @GetMapping
    public List<ParticipantResponse> getParticipants(@PathVariable Long tripId) {
        return participantService.getParticipantsByTrip(tripId);
    }
}
