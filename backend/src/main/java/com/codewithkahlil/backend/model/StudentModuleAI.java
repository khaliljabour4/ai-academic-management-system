package com.codewithkahlil.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-student, per-module AI data:
 * - courseSummary: an AI-generated summary of the course content/paragraph.
 * - conversations: title -> (question -> response), e.g.
 *     {
 *       "Chapter 2 doubts": { "What is inheritance?": "...", "What is polymorphism?": "..." },
 *       "Exam prep": { "Explain recursion": "..." }
 *     }
 *   Stored as one JSON column via ConversationMapConverter since JPA has no
 *   native support for a Map of Maps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "student_module_ai")
public class StudentModuleAI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String courseSummary;

    @Builder.Default
    @Convert(converter = ConversationMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, LinkedHashMap<String, String>> conversations = new LinkedHashMap<>();

    /** Adds one question/response pair under the given conversation title, creating it if new. */
    public void addMessage(String conversationTitle, String question, String response) {
        conversations
                .computeIfAbsent(conversationTitle, k -> new LinkedHashMap<>())
                .put(question, response);
    }
}
