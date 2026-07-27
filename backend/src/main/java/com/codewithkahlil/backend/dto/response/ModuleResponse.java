package com.codewithkahlil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModuleResponse {
    int id ;
    String name ;
    String description ;
    String filiereName ;
    String profName ;
}
