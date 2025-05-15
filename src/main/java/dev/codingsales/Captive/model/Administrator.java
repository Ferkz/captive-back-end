package dev.codingsales.Captive.model;

import jakarta.persistence.*;

@Entity
public class Administrator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    // Getters e Setters
}
