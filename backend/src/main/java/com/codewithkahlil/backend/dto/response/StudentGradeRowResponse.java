package com.codewithkahlil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentGradeRowResponse {
    int studentId;
    String name;
    String prenom;
    Double note1;
    Double note2;
    Double note3;
    Double moyen ;
}
