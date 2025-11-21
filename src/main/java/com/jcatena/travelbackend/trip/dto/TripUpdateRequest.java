package com.jcatena.travelbackend.trip.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)

public class TripUpdateRequest {


    private String name;
    private String description;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
}
