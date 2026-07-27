package com.codewithkahlil.backend.dto.requests;

import lombok.Data;

@Data
public class AskQuestionRequest {
    String conversationTitle;
    String question;
}
