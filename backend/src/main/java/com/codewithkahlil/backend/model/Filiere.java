package com.codewithkahlil.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Filiere = Classe: a group of students following the same program/year
 * (e.g. "2nd year Computer Engineering"). Owns the modules taught to it
 * and its weekly schedule (emploi du temps).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "filiere")
public class Filiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private int year;

    @Builder.Default
    @OneToMany(mappedBy = "filiere")
    private List<Student> students = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Module> modules = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleSlot> schedule = new ArrayList<>();

    /**
     * Builds a day -> (timeSlot -> module) view of the schedule, handy for
     * rendering the emploi du temps grid on the frontend. Not stored as-is -
     * always derived fresh from the relational ScheduleSlot rows above, so
     * the foreign keys (and joins/reports on them) stay real.
     */
    @Transient
    public Map<DayOfWeek, Map<TimeSlot, Module>> getWeeklyScheduleAsMap() {
        Map<DayOfWeek, Map<TimeSlot, Module>> map = new EnumMap<>(DayOfWeek.class);
        for (ScheduleSlot slot : schedule) {
            map.computeIfAbsent(slot.getDayOfWeek(), d -> new EnumMap<>(TimeSlot.class))
                    .put(slot.getTimeSlot(), slot.getModule());
        }
        return map;
    }
}
