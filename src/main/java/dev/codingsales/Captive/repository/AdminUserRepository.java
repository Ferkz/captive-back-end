package dev.codingsales.Captive.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.codingsales.Captive.entity.AdminUser;
@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    /**
     * @param email the email
     * @return the optional
     */
    public Optional<AdminUser> findByEmail(@Param("email")String email);
}
