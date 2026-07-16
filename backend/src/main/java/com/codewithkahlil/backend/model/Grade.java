package com.codewithkahlil.backend.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * One student's grade record for one module in one semester.
 * note1/note2/note3 are the three graded assessments, noteTp is optional
 * (only meaningful when module.hasTp() is true), noteGenerale is the
 * final computed average stored for quick reads on the dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grade")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;

    private Double note1;

    private Double note2;

    private Double note3;

    // Only set when module.isHasTp() is true - leave null otherwise
    private Double noteTp;

    private Double noteGenerale;

    private String semester;

    /**
     * Recomputes noteGenerale from whichever notes are present.
     * Call this after setting note1/note2/note3/noteTp, before saving.
     */
    public void computeNoteGenerale() {
        double sum = 0;
        int count = 0;
        for (Double n : new Double[]{note1, note2, note3, noteTp}) {
            if (n != null) {
                sum += n;
                count++;
            }
        }
        this.noteGenerale = count == 0 ? null : sum / count;
    }
}
