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


    private Double noteGenerale;

    public Double computeNoteGenerale() {
        double sum = 0;
        int count = 0;
        for (Double n : new Double[]{note1, note2, note3}) {
            if (n != null) {
                sum += n;
                count++;
            }
        }
        if (count == 3) this.noteGenerale =sum / count  ;
        return this.noteGenerale ;
    }

}
