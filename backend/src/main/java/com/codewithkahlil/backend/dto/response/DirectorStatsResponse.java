package com.codewithkahlil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectorStatsResponse {
    long filieresCount;
    long professorsCount;
    long studentsCount;
    Double averageGrade;
}
