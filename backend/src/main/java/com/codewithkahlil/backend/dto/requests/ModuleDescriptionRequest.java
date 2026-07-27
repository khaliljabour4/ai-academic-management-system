package com.codewithkahlil.backend.dto.requests;

import lombok.Data;

@Data
public class ModuleDescriptionRequest {
    int moduleId;
    String description;
}
