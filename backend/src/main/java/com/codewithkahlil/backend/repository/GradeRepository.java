package com.codewithkahlil.backend.repository;

import com.codewithkahlil.backend.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade , Integer> {
}
