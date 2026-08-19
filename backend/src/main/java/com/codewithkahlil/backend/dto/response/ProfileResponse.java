package com.codewithkahlil.backend.dto.response;

import com.codewithkahlil.backend.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    int id;
    String name;
    String prenom;
    String email;
    Role role;
    String cni;
    String cne;
    String filiereName;
    String number;
    String department;
}
