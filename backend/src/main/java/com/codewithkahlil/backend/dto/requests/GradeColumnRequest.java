package com.codewithkahlil.backend.dto.requests;

import lombok.Data;

import java.util.List;

/**
 * Submits one whole column (note1, note2, or note3) for every student in
 * a module at once. column is 1, 2, or 3.
 */
@Data
public class GradeColumnRequest {
    int moduleId;
    int column;
    List<GradeEntry> entries;
}
