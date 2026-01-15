package com.jcatena.travelbackend.participant.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ParticipantRequest {

    @NotBlank(message = "Participant name is required")
    @Size(max = 100, message = "Participant name must be at most 100 characters")
    private String name;
}
