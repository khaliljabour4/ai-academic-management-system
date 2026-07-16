package com.codewithkahlil.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
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

    @Builder.Default
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    //orphanremoval to true mean delete the message from the db if i delete them form list
    //if it s not there he will just change the foreign keuy to null
    private List<Conversation> conversations = new ArrayList<>();
}
