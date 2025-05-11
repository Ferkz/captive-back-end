package dev.codingsales.Captive.entity;

import java.sql.Timestamp;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class Administrator.
 */

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor

public class BaseUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The username. */
    @Column(name = "email")
    @NotNull
    @NotEmpty
    @NotBlank
    private String email;

    /** The password. */
    @Column(name = "password")
    @NotNull
    @NotEmpty
    @NotBlank
    private String password;

    /** The email. */
    @Column(name = "full_name")
    private String fullName;

    /** The creation date. */
    @Column(name = "creation_date")
    private Timestamp creationDate;

    /** The last modification. */
    @Column(name = "last_modification")
    private Timestamp lastModification;

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof AdminUser) {
            AdminUser other = (AdminUser) obj;
            return Objects.equals(this.getId(), other.getId()) && Objects.equals(this.getEmail(), other.getEmail());
        }

        return false;

    }
}
