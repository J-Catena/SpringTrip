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
public class TripSettlementResponse {

    private Long tripId;
    private String tripName;

    private List<PaymentInstruction> payments;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentInstruction {
        private Long payerId;
        private String payerName;
        private Long receiverId;
        private String receiverName;
        private BigDecimal amount;
    }
}
