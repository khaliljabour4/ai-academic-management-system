package com.codewithkahlil.backend.dto.response;

import com.codewithkahlil.backend.model.Module;
import com.codewithkahlil.backend.model.Professor;
import com.codewithkahlil.backend.model.Student;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
@Builder
public class OverviewFiliereResponse {

    List<String> professors ;
    List<String> modules;
    List<StudentResponse> etudiants;

}
