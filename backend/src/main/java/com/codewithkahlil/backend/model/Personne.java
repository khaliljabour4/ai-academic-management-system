package com.codewithkahlil.backend.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Personne {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int id ;

    private String name ;

    private String prenom ;

    private Date date_naissance;


}
