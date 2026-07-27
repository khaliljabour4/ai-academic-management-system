package com.codewithkahlil.backend.dto.requests;

import lombok.Data;

/** One student's value for a single grade column. */
@Data
public class GradeEntry {
    int studentId;
    Double value;
}
