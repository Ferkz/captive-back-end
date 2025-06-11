package dev.codingsales.Captive.repository;

import dev.codingsales.Captive.entity.TermsAndPrivacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermsAndPrivacyRepository extends JpaRepository<TermsAndPrivacy, Long> {
    Optional<TermsAndPrivacy> findByType(String type);
}
