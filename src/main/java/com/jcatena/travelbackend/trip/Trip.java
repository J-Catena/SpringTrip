package com.jcatena.travelbackend.trip;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import com.jcatena.travelbackend.participant.Participant;
import com.jcatena.travelbackend.expense.Expense;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String description;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(length = 3, nullable = false)
    private String currency; // EUR, USD...

    private LocalDate startDate;
    private LocalDate endDate;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();
}
