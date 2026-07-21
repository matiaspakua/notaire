package com.licensis.notaire.config;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import com.licensis.notaire.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Siembra el usuario por defecto {@code admin}/{@code admin} una única vez, si no
 * existe ninguno.
 *
 * <p>La contraseña se almacena como hash BCrypt (issue #554), coincidiendo con la
 * verificación que realiza {@code UsuarioController#login}. Es un seed de una sola
 * vez: si el usuario ya existe (sembrado por Flyway V2, o porque un operador ya
 * rotó su contraseña) no se toca — de lo contrario cualquier cambio de credenciales
 * quedaría deshecho en cada reinicio del backend (issue #553).</p>
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS_PLAIN = "admin";

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
            log.warn("No se pudo garantizar el usuario inicial 'admin': {}", e.getMessage());
        }
    }

    private void ensureAdminUser() {
        if (usuarioRepository.findByNombre(ADMIN_USER).isPresent()) {
            log.debug("Usuario 'admin' ya existe; no se modifican sus credenciales.");
            return;
        }

        log.info("Usuario 'admin' no encontrado. Creando usuario por defecto admin/admin...");
        Persona adminPersona = buildAdminPersona();
        personaRepository.save(adminPersona);

        Usuario admin = new Usuario();
        admin.setNombre(ADMIN_USER);
        admin.setContrasenia(passwordEncoder.encode(ADMIN_PASS_PLAIN));
        admin.setEstado(true);
        admin.setTipo("Escribano");
        admin.setFkIdPersona(adminPersona);
        usuarioRepository.save(admin);
        log.info("Usuario inicial 'admin' creado correctamente.");
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
