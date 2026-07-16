package com.codewithkahlil.backend.repository;

import com.codewithkahlil.backend.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendenceRepository extends JpaRepository<Attendance , Integer> {
}
