package com.codewithkahlil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountCreatedResponse {
    String email;
    String password;
}
