package com.codewithkahlil.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;

/**
 * One fixed weekly slot on a Filiere's emploi du temps: a (day, timeSlot)
 * pair pointing to the Module taught then. Monday-Saturday x
 * (MORNING|AFTERNOON) gives at most 12 rows per Filiere.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "schedule_slot")
public class ScheduleSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    private TimeSlot timeSlot;
}
