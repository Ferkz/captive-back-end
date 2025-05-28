package dev.codingsales.Captive;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import dev.codingsales.Captive.entity.AdminUser;
import dev.codingsales.Captive.entity.Privilege;
import dev.codingsales.Captive.entity.Role;
import dev.codingsales.Captive.repository.AdminUserRepository;
import dev.codingsales.Captive.repository.PrivilegeRepository;
import dev.codingsales.Captive.repository.RoleRepository;

import javax.transaction.Transactional;

@ConditionalOnProperty(prefix = "jespresso.datasource.data", name = "initialize", matchIfMissing = false, havingValue = "true")
@Component
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent>{
    /** The already setup. */
    boolean alreadySetup = false;

    /** The admin user repository. */
    @Autowired
    private AdminUserRepository adminUserRepository;

    /** The role repository. */
    @Autowired
    private RoleRepository roleRepository;

    /** The privilege repository. */
    @Autowired
    private PrivilegeRepository privilegeRepository;

    /** The password encoder. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * On application event.
     *
     * @param event the event
     */
    @Override
    @javax.transaction.Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Verifica se as tabelas de administradores estão vazias.
        // Para adicionar novos usuários, você pode temporariamente setar 'alreadySetup = false;'
        // OU verificar se o usuário específico que você quer adicionar já existe.
        alreadySetup = !adminUserRepository.findAll().isEmpty();

        if (!alreadySetup) {
            Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
            Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");

            List<Privilege> adminPrivileges = Arrays.asList(readPrivilege, writePrivilege);
            createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
            createRoleIfNotFound("ROLE_USER", Arrays.asList(readPrivilege));

            Role adminRole = roleRepository.findByName("ROLE_ADMIN").get();

            if (adminUserRepository.findByEmail("admin@localhost").isEmpty()) {
                AdminUser defaultAdmin = new AdminUser();
                defaultAdmin.setFullName("Default Administrator");
                defaultAdmin.setPassword(passwordEncoder.encode("password")); // Senha padrão 'password'
                defaultAdmin.setEmail("admin@localhost");
                defaultAdmin.setRoles(Arrays.asList(adminRole));
                defaultAdmin.setEnabled(true);
                adminUserRepository.save(defaultAdmin);
            }

            String preExistingUserEmail = "meu.admin.antigo@dominio.com"; // <-- SEU E-MAIL
            if (adminUserRepository.findByEmail(preExistingUserEmail).isEmpty()) {
                AdminUser preExistingAdmin = new AdminUser();
                preExistingAdmin.setFullName("Administrador"); // <-- SEU NOME
                preExistingAdmin.setPassword(passwordEncoder.encode("minhaSenhaAntigaSecreta!")); //
                preExistingAdmin.setEmail(preExistingUserEmail);
                preExistingAdmin.setRoles(Arrays.asList(adminRole)); // Atribui a ROLE_ADMIN
                preExistingAdmin.setEnabled(true); // Ativa o usuário
                adminUserRepository.save(preExistingAdmin);
            }

            alreadySetup = true; // Marca como setup completo
        }
    }

    /**
     *
     * @param name the name
     * @return the privilege
     */
    @javax.transaction.Transactional
    private Privilege createPrivilegeIfNotFound(String name) {
        Optional<Privilege> privilegeFromDb = privilegeRepository.findByName(name);
        if (!privilegeFromDb.isPresent()) {
            Privilege privilege = new Privilege(null, name, null);
            return privilegeRepository.save(privilege);
        }
        return privilegeFromDb.get();
    }

    /**
     * Creates the role if not found.
     *
     * @param name       the name
     * @param privileges the privileges
     * @return the role
     */
    @Transactional
    private Role createRoleIfNotFound(String name, Collection<Privilege> privileges) {
        Optional<Role> roleFromDb = roleRepository.findByName(name);
        if (!roleFromDb.isPresent()) {
            Role role = new Role();
            role.setName(name);
            role.setPrivileges(privileges);
            return roleRepository.save(role);
        }
        return roleFromDb.get();
    }
}