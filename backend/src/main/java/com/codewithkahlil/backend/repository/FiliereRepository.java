package com.codewithkahlil.backend.repository;

import com.codewithkahlil.backend.model.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FiliereRepository extends JpaRepository<Filiere , Integer> {

    Filiere findByName(String name) ;
    Filiere findById(int id);
}
