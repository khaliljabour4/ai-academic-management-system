package com.codewithkahlil.backend.repository;

import com.codewithkahlil.backend.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository <Student , Integer> {
    Student findByCne(String cne);
    Student findById(int id);

    // Same reasoning as ProfessorRepository.findByUser_Email
    Student findByUser_Email(String email);

    List<Student> findByFiliere_Id(int filiereId);
}
