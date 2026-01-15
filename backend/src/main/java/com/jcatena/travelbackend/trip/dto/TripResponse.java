package com.jcatena.travelbackend.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {

    private Long id;
    private String name;
    private String description;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long ownerId;
}
