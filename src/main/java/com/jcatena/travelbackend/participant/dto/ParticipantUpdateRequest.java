package com.jcatena.travelbackend.participant.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParticipantUpdateRequest {

    @Size(max = 100, message = "Participant name must be at most 100 characters")
    private String name;
}
