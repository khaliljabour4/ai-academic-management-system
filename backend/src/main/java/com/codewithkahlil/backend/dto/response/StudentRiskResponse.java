package com.codewithkahlil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentRiskResponse {
    String name;
    String prenom;
    Double successRate;
    boolean atRisk;
}
