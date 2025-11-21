package com.jcatena.travelbackend.participant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ParticipantUpdateRequest {

    private String name;
    private String email;
}
