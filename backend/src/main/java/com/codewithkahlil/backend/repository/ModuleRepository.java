package com.codewithkahlil.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.codewithkahlil.backend.model.Module;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module , Integer> {
    Module findById(int id);
    List<Module> findByTeacher_Id(int teacherId);
    List<Module> findByFiliere_Id(int filiereId);
}
