package com.codewithkahlil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DirectorConversationResponse {
    int id;
    String title;
    String studentName;
    String moduleName;
    List<String> messages;
}
