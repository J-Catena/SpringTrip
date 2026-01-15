package com.jcatena.travelbackend.trip.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TripUpdateRequest {

    @Size(max = 100, message = "Trip name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter code (e.g. EUR, USD)")
    private String currency;

    private LocalDate startDate;
    private LocalDate endDate;
}
