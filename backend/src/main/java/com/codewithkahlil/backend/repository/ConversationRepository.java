package com.codewithkahlil.backend.repository;

import com.codewithkahlil.backend.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation , Integer> {
}
