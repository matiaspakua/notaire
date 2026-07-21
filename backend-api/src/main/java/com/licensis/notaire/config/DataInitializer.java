package com.licensis.notaire.config;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import com.licensis.notaire.repository.UsuarioRepository;
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
 * coincidiendo con la verificación que realiza {@code UsuarioController#login}.
 * Es un seed de una sola vez: si el usuario ya existe (sembrado por Flyway V2, o
 * porque un operador ya rotó su contraseña) no se toca — de lo contrario cualquier
 * cambio de credenciales quedaría deshecho en cada reinicio del backend (issue
 * #553).</p>
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin}")
    private String adminPassword;

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PersonaRepository personaRepository,
                           TipoIdentificacionRepository tipoIdentificacionRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
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
        if (usuarioRepository.findByNombre(adminUsername).isPresent()) {
            log.debug("Usuario '{}' ya existe; no se modifican sus credenciales.", adminUsername);
            return;
        }

        log.info("Usuario '{}' no encontrado. Creando usuario administrador inicial...", adminUsername);
        Persona adminPersona = buildAdminPersona();
        personaRepository.save(adminPersona);

        Usuario admin = new Usuario();
        admin.setNombre(adminUsername);
        admin.setContrasenia(passwordEncoder.encode(adminPassword));
        admin.setEstado(true);
        admin.setTipo("Escribano");
        admin.setFkIdPersona(adminPersona);
        usuarioRepository.save(admin);
        log.info("Usuario administrador inicial '{}' creado correctamente.", adminUsername);
    }

    private Persona buildAdminPersona() {
        TipoIdentificacion tipo = tipoIdentificacionRepository.findAll().stream()
                .findFirst()
                .orElseGet(this::createDefaultTipoIdentificacion);

        Persona persona = new Persona();
        persona.setNombre("Admin");
        persona.setApellido("Sistema");
        persona.setEsCliente(false);
        persona.setNumeroIdentificacion("00000000");
        persona.setFkIdTipoIdentificacion(tipo);
        return persona;
    }

    private TipoIdentificacion createDefaultTipoIdentificacion() {
        TipoIdentificacion tipo = new TipoIdentificacion();
        tipo.setNombre("DNI");
        return tipoIdentificacionRepository.save(tipo);
    }

}
