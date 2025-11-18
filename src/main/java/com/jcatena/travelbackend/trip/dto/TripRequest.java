package com.jcatena.travelbackend.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TripRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency; // EUR, USD...

    private LocalDate startDate;
    private LocalDate endDate;
}
