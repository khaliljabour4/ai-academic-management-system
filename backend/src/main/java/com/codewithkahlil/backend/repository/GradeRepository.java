package com.codewithkahlil.backend.repository;

import com.codewithkahlil.backend.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade , Integer> {
    Grade findByStudent_IdAndModule_Id(int studentId, int moduleId);
    List<Grade> findByModule_Id(int moduleId);
}
