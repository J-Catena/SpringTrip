package com.jcatena.travelbackend.expense;

import com.jcatena.travelbackend.participant.Participant;
import com.jcatena.travelbackend.trip.Trip;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal amount;

    private String description;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payer_id", nullable = false)
    private Participant payer;
}
