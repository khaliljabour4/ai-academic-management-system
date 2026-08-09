package com.codewithkahlil.backend.dto.requests;

import lombok.Data;

import java.util.List;


@Data
public class GradeColumnRequest {
    int moduleId;
    int column;
    List<GradeEntry> entries;
}
