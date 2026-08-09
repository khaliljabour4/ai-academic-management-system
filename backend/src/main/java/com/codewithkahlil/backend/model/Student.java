package com.codewithkahlil.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)

 /*ecxplanation : Lombok's @Data (which includes @EqualsAndHashCode) only
 looks at fields declared in that specific class by default — it ignores
 inherited fields. For Student extends Personne, that means without callSuper = true,
  two different Student objects with different id/name (inherited from Personne)
  but the same filiere/grades would be considered "equal." callSuper = true tells
  Lombok to also include the parent's fields in the equality check — important for
  entities where the inherited id is really what should distinguish two rows.*/


@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "student")
@DiscriminatorValue("STUDENT")
public class Student extends Personne {

    private String cni ;

    private String cne ;


    @ManyToOne
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    @Builder.Default
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Grade> grades = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances = new ArrayList<>();
}
