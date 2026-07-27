package com.codewithkahlil.backend.controler;

import com.codewithkahlil.backend.dto.response.DirectorConversationResponse;
import com.codewithkahlil.backend.model.Conversation;
import com.codewithkahlil.backend.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/director")
@RequiredArgsConstructor
public class DirectorController {

    private final ConversationRepository conversationRepository;

    /** Every AI conversation, across every student and module - director oversight. */
    @GetMapping("/conversations")
    ResponseEntity<?> getAllConversations() {
        try {
            List<Conversation> conversations = conversationRepository.findAll();

            List<DirectorConversationResponse> response = conversations.stream().map(c ->
                    DirectorConversationResponse.builder()
                            .id(c.getId())
                            .title(c.getTitle())
                            .studentName(c.getStudent().getName() + " " + c.getStudent().getPrenom())
                            .moduleName(c.getModule().getName())
                            .messages(c.getMessages())
                            .build()
            ).toList();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
