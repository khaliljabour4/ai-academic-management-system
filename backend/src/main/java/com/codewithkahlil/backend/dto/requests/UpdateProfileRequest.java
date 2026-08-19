package com.codewithkahlil.backend.dto.requests;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String prenom;
    private String currentPassword;
    private String newPassword;
}
