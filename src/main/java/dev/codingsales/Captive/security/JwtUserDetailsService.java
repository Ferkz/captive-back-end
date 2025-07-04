package dev.codingsales.Captive.security;

import java.util.*;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.codingsales.Captive.entity.AdminUser;
import dev.codingsales.Captive.entity.Privilege;
import dev.codingsales.Captive.entity.Role;
import dev.codingsales.Captive.exeption.NoContentException;
import dev.codingsales.Captive.repository.RoleRepository;
import dev.codingsales.Captive.service.AdministratorService;

@Component
public class JwtUserDetailsService implements UserDetailsService {
    /** The administrator service. */
    private static final Logger logger = LoggerFactory.getLogger(JwtUserDetailsService.class);
    @Autowired
    private AdministratorService administratorService;

    /** The role repository. */
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Load user by username.
     *
     * @param username the username
     * @return the user details
     * @throws UsernameNotFoundException the username not found exception
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            AdminUser administrator = this.administratorService.getAdministratorByEmail(username);
            return new User(administrator.getEmail(), administrator.getPassword(), true, true, true, true,
                    this.getAuthorities(administrator.getRoles()));
        } catch (NoContentException e) {
            logger.warn("Usuario '{}' não encontrado, tentando carregar com ROLE_USER");
            Optional<Role> defaultRoleOpt = roleRepository.findByName("ROLE_USER");
            if (defaultRoleOpt.isPresent()){
                return new org.springframework.security.core.userdetails.User(
                        " ", " ", true, true, true, true, // Usuário dummy
                        getAuthorities(Arrays.asList(defaultRoleOpt.get())));
            } else {
                logger.error("Usuario '{}' nao encontrado e a role padrao 'ROLE_USER' não existe no banco ");
                throw new UsernameNotFoundException("Usuario nao encontrado e role 'ROLE_USER' nao existe no banco");
            }
        }
    }

    /**
     * Gets the authorities.
     *
     * @param roles the roles
     * @return the authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(
            Collection<Role> roles) {

        return getGrantedAuthorities(getPrivileges(roles));
    }

    /**
     * Gets the privileges.
     *
     * @param roles the roles
     * @return the privileges
     */
    private List<String> getPrivileges(Collection<Role> roles) {

        List<String> privileges = new ArrayList<>();
        List<Privilege> collection = new ArrayList<>();
        for (Role role : roles) {
            collection.addAll(role.getPrivileges());
        }
        for (Privilege item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }
    /**
     * Gets the granted authorities.
     *
     * @param privileges the privileges
     * @return the granted authorities
     */
    private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }
}
