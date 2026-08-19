package com.codewithkahlil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FiliereSummaryResponse {
    int filiereId;
    String filiereName;
    int studentsCount;
    int professorsCount;
    Double averageGrade;
    int atRiskCount;
    int gradedCount;
}
