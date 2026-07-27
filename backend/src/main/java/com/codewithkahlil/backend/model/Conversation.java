package com.codewithkahlil.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * One AI conversation thread for a student, on a module.
 * messages is a flat, ordered list of strings: question, response,
 * question, response... (even index = question, odd index = response).
 * Simpler than a separate Message entity - one column, no extra table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "conversation")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "conversation_messages", joinColumns = @JoinColumn(name = "conversation_id"))
    @OrderColumn(name = "message_order")
    @Column(name = "content", columnDefinition = "TEXT")
    private List<String> messages = new ArrayList<>();

    /** Appends one question then its response, keeping order. */
    public void addMessage(String question, String response) {
        messages.add(question);
        messages.add(response);
    }
}
