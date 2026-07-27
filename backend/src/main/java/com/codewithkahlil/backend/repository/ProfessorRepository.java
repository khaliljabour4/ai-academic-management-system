package com.codewithkahlil.backend.repository;

import com.codewithkahlil.backend.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorRepository extends JpaRepository <Professor , Integer>{

    Professor findById(int id );

    // Used to find "which Professor is this logged-in User" - the login
    // account (User) and the person record (Professor/Personne) are two
    // separate tables linked by Personne.user, so we look it up by email.
    Professor findByUser_Email(String email);
}
