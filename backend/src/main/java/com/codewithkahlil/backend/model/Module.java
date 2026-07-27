package com.codewithkahlil.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "module")
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String description ;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Professor teacher;

    @ManyToOne
    @JoinColumn(name = "filiere_id")
    @JsonIgnore
    private Filiere filiere;

    @Builder.Default
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conversation> conversations = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;


    @Enumerated(EnumType.STRING)
    private TimeSlot timeSlot;
}
