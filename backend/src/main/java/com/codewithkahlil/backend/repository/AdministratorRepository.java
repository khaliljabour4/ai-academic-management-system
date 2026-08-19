package com.codewithkahlil.backend.repository;

import com.codewithkahlil.backend.model.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator , Integer> {
    Administrator findByUser_Email(String email);
}
