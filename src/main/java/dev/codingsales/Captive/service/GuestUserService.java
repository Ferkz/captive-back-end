package dev.codingsales.Captive.service;

import dev.codingsales.Captive.entity.GuestUser;
import org.springframework.stereotype.Service;

import java.util.Optional;


public interface GuestUserService {
    /**
     * @param cpf O cpf a ser buscado
     * @return
     */
    Optional<GuestUser> findByCpf(String cpf);
    /**
     * @param guestUser O objeto GuestUser a ser salvo.
     * @return O objeto GuestUser salvo.
     */
    GuestUser save(GuestUser guestUser);
}
