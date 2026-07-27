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
public class AuthenticationResponse {

    private String token ;

    private Role role ;
}
