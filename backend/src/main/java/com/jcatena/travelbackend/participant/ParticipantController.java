package com.jcatena.travelbackend.participant;

import com.jcatena.travelbackend.common.exceptions.NotFoundException;
import com.jcatena.travelbackend.participant.dto.ParticipantRequest;
import com.jcatena.travelbackend.participant.dto.ParticipantResponse;
import com.jcatena.travelbackend.participant.dto.ParticipantUpdateRequest;
import com.jcatena.travelbackend.user.User;
import com.jcatena.travelbackend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;
    private final UserRepository userRepository;

    private Long getCurrentUserId(UserDetails principal) {
        String email = principal.getUsername(); // Spring usa username, tú lo usas como email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return user.getId();
    }

    @PostMapping
    public ParticipantResponse addParticipant(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ParticipantRequest request
    ) {
        Long userId = getCurrentUserId(principal);
        return participantService.addParticipantToTrip(tripId, userId, request);
    }

    @GetMapping
    public List<ParticipantResponse> getParticipants(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = getCurrentUserId(principal);
        return participantService.getParticipantsByTrip(tripId, userId);
    }

    @PutMapping("/{participantId}")
    public ParticipantResponse updateParticipant(
            @PathVariable Long tripId,
            @PathVariable Long participantId,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ParticipantUpdateRequest request
    ) {
        Long userId = getCurrentUserId(principal);
        return participantService.updateParticipant(tripId, participantId, userId, request);
    }
}
