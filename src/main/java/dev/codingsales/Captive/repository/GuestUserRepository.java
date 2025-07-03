package dev.codingsales.Captive.repository;

import dev.codingsales.Captive.entity.GuestUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuestUserRepository extends JpaRepository<GuestUser, Long> {
    Optional<GuestUser> findByCpf(String cpf);
}
