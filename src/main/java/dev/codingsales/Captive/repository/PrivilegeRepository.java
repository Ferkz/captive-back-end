package dev.codingsales.Captive.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.codingsales.Captive.entity.Privilege;
@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    /**
     * @param name
     * @return
     */
    Optional<Privilege> findByName(String name);
}
