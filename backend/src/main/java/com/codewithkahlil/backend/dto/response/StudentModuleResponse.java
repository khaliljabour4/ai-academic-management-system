package com.codewithkahlil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentModuleResponse {
    int moduleId;
    String name;
    String description;
    String teacherName;
    Double note1;
    Double note2;
    Double note3;
    Double noteGenerale;
}
