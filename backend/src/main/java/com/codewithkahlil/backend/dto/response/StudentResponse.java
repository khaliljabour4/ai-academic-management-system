package com.codewithkahlil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentResponse {
    int id ;
    String name ;
    String prenom ;
    String email ;
    String cni ;
    String cne ;
    int filiereId ;
    String filiereName ;
}
