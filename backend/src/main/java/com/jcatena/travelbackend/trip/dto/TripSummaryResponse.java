package com.jcatena.travelbackend.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripSummaryResponse {

    private Long tripId;
    private String tripName;
    private BigDecimal totalAmount;

    private List<ParticipantSummary> participants;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParticipantSummary {
        private Long id;
        private String name;
        private BigDecimal totalPaid;
        private BigDecimal balance;   // ← NUEVO
    }
}
