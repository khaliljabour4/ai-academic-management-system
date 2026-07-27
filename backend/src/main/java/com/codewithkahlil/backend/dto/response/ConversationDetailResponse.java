package com.codewithkahlil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConversationDetailResponse {
    int id;
    String title;
    // Flat, ordered list: question, response, question, response...
    List<String> messages;
}
