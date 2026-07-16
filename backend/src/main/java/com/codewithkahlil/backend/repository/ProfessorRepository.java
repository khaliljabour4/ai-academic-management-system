package com.codewithkahlil.backend.repository;

import com.codewithkahlil.backend.model.Professor;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorRepository extends JpaRepository <Professor , Integer>{
}
