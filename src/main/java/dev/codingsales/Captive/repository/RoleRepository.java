package dev.codingsales.Captive.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.codingsales.Captive.entity.Role;
@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
    /**
     * @param string
     * @return
     */
    Optional<Role> findByName(String string);
}
