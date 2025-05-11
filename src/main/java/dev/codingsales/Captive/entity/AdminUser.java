package dev.codingsales.Captive.entity;

import java.util.Collection;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * The Class Administrator.
 */
@Entity
@Table(name="administrators")

public class AdminUser extends BaseUser {
    /** The enabled. */
    @Column(name="enabled")
    private Boolean enabled;

    /** The roles. */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;


    /**
     * Gets the enabled.
     *
     * @return the enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled.
     *
     * @param enabled the new enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Inits the.
     *
     * @param id the id
     * @param email the email
     * @param password the password
     * @param fullName the full name
     * @return the abstract user
     */
    public AdminUser init(long id, String email, String password, String fullName) {
        this.setId(id);
        this.setEmail(email);
        this.setFullName(fullName);
        this.setPassword(password);
        return this;
    }

    /**
     * Gets the roles.
     *
     * @return the roles
     */
    public Collection<Role> getRoles() {
        return roles;
    }

    /**
     * Sets the roles.
     *
     * @param roles the new roles
     */
    public void setRoles(Collection<Role> roles) {
        this.roles = roles;
    }
}
