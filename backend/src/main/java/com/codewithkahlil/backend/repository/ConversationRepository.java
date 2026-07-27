package com.codewithkahlil.backend.repository;

import com.codewithkahlil.backend.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation , Integer> {
    List<Conversation> findByStudent_IdAndModule_Id(int studentId, int moduleId);
    Conversation findByStudent_IdAndModule_IdAndTitle(int studentId, int moduleId, String title);
    // findAll() (inherited) is what the Director uses to see every conversation
}
