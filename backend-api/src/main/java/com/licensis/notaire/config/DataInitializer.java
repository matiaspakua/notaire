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
 * Siembra el usuario administrador inicial una única vez, si no existe ninguno.
 *
 * <p>Username y password son configurables vía {@code APP_ADMIN_USER}/
 * {@code APP_ADMIN_PASSWORD} (issue #651); por defecto {@code admin}/{@code admin}
 * en desarrollo. La contraseña se almacena como hash BCrypt (issue #554),
 * coincidiendo con la verificación que realiza {@code UserController#login}.
 * Es un seed de una sola vez: si el usuario ya existe (sembrado por Flyway V2, o
 * porque un operador ya rotó su contraseña) no se toca — de lo contrario cualquier
 * cambio de credenciales quedaría deshecho en cada reinicio del backend (issue
 * #553).</p>
 *
 * <p>Fuera de los entornos {@code development}/{@code dev}/{@code local}/{@code test}
 * no se siembra el usuario si la contraseña está vacía o es el valor por defecto
 * conocido {@code admin}: se registra un error y se omite el seed, de modo que
 * ningún camino de arranque (jar, compose, staging) publique {@code admin/admin}
 * (issue #1249).</p>
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
            log.warn("No se pudo garantizar el usuario inicial '{}': {}", adminUsername, e.getMessage());
        }
    }

    private void ensureAdminUser() {
        if (isDefaultPassword() && !allowsDefaultSeed()) {
            log.error("APP_ADMIN_PASSWORD no configurada o con valor por defecto en el entorno '{}': "
                    + "se omite la creación del usuario '{}' (issue #1249).", environment, adminUsername);
            return;
        }
        if (userRepository.findByName(adminUsername).isPresent()) {
            log.debug("Usuario '{}' ya existe; no se modifican sus credenciales.", adminUsername);
            return;
        }

        log.info("Usuario '{}' no encontrado. Creando usuario administrador inicial...", adminUsername);
        Person adminPerson = buildAdminPerson();
        personRepository.save(adminPerson);

        User admin = new User();
        admin.setName(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setStatus(true);
        admin.setType("Escribano");
        admin.setFkIdPerson(adminPerson);
        userRepository.save(admin);
        log.info("Usuario administrador inicial '{}' creado correctamente.", adminUsername);
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
