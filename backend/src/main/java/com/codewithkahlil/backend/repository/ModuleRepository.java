package com.codewithkahlil.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.codewithkahlil.backend.model.Module;

@Repository
public interface ModuleRepository extends JpaRepository<Module , Integer> {
}
