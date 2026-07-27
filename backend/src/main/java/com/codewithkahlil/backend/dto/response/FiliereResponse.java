package com.codewithkahlil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FiliereResponse {
    String name ;
    String description ;
    int id ;
}
