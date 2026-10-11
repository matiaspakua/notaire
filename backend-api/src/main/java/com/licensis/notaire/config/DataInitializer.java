package com.licensis.notaire.config;

import java.util.Locale;
import java.util.Set;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the initial administrator once, when that user does not exist yet.
 *
 * <p>Username and password come from {@code APP_ADMIN_USER}/
 * {@code APP_ADMIN_PASSWORD} (issue #651); the default is {@code admin}/{@code admin}
 * in development. The password is stored as a BCrypt hash (issue #554),
 * which is what {@code UserController#login} verifies.
 * The seed runs once: if the user already exists (Flyway V2, or an operator
 * already rotated the password) it is left unchanged. Otherwise a restart
 * would undo a credential change (issue #553).</p>
 *
 * <p>Outside {@code development}/{@code dev}/{@code local}/{@code test},
 * the seed is skipped when the password is empty or the known default
 * {@code admin}. An error is logged so no startup path (jar, compose, staging)
 * publishes {@code admin/admin} (issue #1249).</p>
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String KNOWN_DEFAULT_PASSWORD = "admin";
    private static final Set<String> SEED_DEFAULT_ENVIRONMENTS = Set.of("development", "dev", "local", "test");

    @Value("${app.environment:development}")
    private String environment;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin}")
    private String adminPassword;

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final IdentificationTypeRepository identificationTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PersonRepository personRepository,
                           IdentificationTypeRepository identificationTypeRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.identificationTypeRepository = identificationTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            ensureAdminUser();
        } catch (Exception e) {
            log.warn("Could not ensure the initial user '{}': {}", adminUsername, e.getMessage());
        }
    }

    private void ensureAdminUser() {
        if (isDefaultPassword() && !allowsDefaultSeed()) {
            log.error("APP_ADMIN_PASSWORD is missing or still the default in environment '{}': "
                    + "skipping creation of user '{}' (issue #1249).", environment, adminUsername);
            return;
        }
        if (userRepository.findByName(adminUsername).isPresent()) {
            log.debug("User '{}' already exists; credentials are left unchanged.", adminUsername);
            return;
        }

        log.info("User '{}' was not found. Creating the initial administrator...", adminUsername);
        Person adminPerson = buildAdminPerson();
        personRepository.save(adminPerson);

        User admin = new User();
        admin.setName(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setStatus(true);
        admin.setType("Escribano");
        admin.setFkIdPerson(adminPerson);
        userRepository.save(admin);
        log.info("Initial administrator '{}' was created.", adminUsername);
    }

    private Person buildAdminPerson() {
        IdentificationType type = identificationTypeRepository.findAll().stream()
                .findFirst()
                .orElseGet(this::createDefaultIdentificationType);

        Person person = new Person();
        person.setFirstName("Admin");
        person.setLastName("Sistema");
        person.setIsClient(false);
        person.setIdentificationNumber("00000000");
        person.setFkIdIdentificationType(type);
        return person;
    }

    private IdentificationType createDefaultIdentificationType() {
        IdentificationType type = new IdentificationType();
        type.setName("DNI");
        return identificationTypeRepository.save(type);
    }


    private boolean isDefaultPassword() {
        return adminPassword == null || adminPassword.isBlank() || KNOWN_DEFAULT_PASSWORD.equals(adminPassword);
    }

    private boolean allowsDefaultSeed() {
        return environment != null && SEED_DEFAULT_ENVIRONMENTS.contains(environment.trim().toLowerCase(Locale.ROOT));
    }
}
