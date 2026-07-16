package com.codewithkahlil.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Base entity for a real person in the system (Director, Administrator,
 * Professor, Student). Uses JOINED inheritance: this class owns the
 * "personne" table (shared identity columns), each subclass owns its own
 * table with only its extra fields, linked by a shared id.
 *
 * This is separate from User: User is the login/auth account (email,
 * password, role) used by Spring Security. Each Personne subtype holds a
 * one-to-one link to its own User, so "being a person" and "having login
 * credentials" stay two separate concerns.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "personne")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Personne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String prenom;

    private LocalDate dateNaissance;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
