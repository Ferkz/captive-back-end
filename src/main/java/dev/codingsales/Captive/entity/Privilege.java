package dev.codingsales.Captive.entity;
import java.util.Collection;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class Privilege.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Privilege {
    /** The id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** The name. */
    private String name;
    /** The roles. */
    @ManyToMany(mappedBy = "privileges")
    private Collection<Role> roles;
}
