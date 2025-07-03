package dev.codingsales.Captive.service.impl;

import dev.codingsales.Captive.repository.GuestUserRepository;
import dev.codingsales.Captive.entity.GuestUser;
import dev.codingsales.Captive.service.GuestUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class GuestUserServiceImpl implements GuestUserService{
    private final GuestUserRepository guestUserRepository;
    @Autowired
    public GuestUserServiceImpl(GuestUserRepository guestUserRepository) {
        this.guestUserRepository = guestUserRepository;
    }
    @Override
    public Optional<GuestUser> findByCpf(String cpf) {
        return guestUserRepository.findByCpf(cpf);
    }
    @Override
    public GuestUser save(GuestUser guestUser) {
        return guestUserRepository.save(guestUser);
    }
}
