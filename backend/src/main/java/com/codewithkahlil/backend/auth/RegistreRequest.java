package com.codewithkahlil.backend.auth;

import com.codewithkahlil.backend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegistreRequest {
    private String email ;
    private String password ;
    private Role role ;
}
