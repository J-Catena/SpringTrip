package com.jcatena.travelbackend.participant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParticipantRequest {

    @NotBlank
    private String name;

    @Email
    private String email;
}
