package com.codewithkahlil.backend.repository;

import com.codewithkahlil.backend.model.Director;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectorRepository extends JpaRepository<Director , Integer> {
    Director findByUser_Email(String email);
}
