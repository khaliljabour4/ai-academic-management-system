package com.codewithkahlil.backend.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * A course/subject taught to one Filiere by one Professor.
 * "hasTp" flags whether this module carries a practical-work grade -
 * if false, the TP note on each Grade for this module should stay null.
 */
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

    @Builder.Default
    private boolean hasTp = false;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Professor teacher;

    @ManyToOne
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;
}
