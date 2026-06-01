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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Garantiza que el usuario por defecto {@code admin}/{@code admin} exista y pueda
 * iniciar sesión en cada arranque del backend.
 *
 * <p>La contraseña se almacena como hash MD5, coincidiendo con la verificación que
 * realiza {@code UsuarioController#login}. Es idempotente: si el usuario ya existe
 * (por ejemplo, sembrado por {@code init-db}) se actualiza su contraseña y estado;
 * si no existe, se crea junto con su persona asociada. Esto asegura el acceso por
 * defecto incluso sobre un volumen de base de datos preexistente, donde los scripts
 * de {@code init-db} no se vuelven a ejecutar.</p>
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS_PLAIN = "admin";

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PersonaRepository personaRepository,
                           TipoIdentificacionRepository tipoIdentificacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
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
        Optional<Usuario> existing = usuarioRepository.findByNombre(ADMIN_USER);
        if (existing.isPresent()) {
            Usuario admin = existing.get();
            admin.setContrasenia(md5(ADMIN_PASS_PLAIN));
            admin.setEstado(true);
            usuarioRepository.save(admin);
            log.info("Usuario 'admin' verificado: contraseña por defecto y estado activo asegurados.");
            return;
        }

        log.info("Usuario 'admin' no encontrado. Creando usuario por defecto admin/admin...");
        Persona adminPersona = buildAdminPersona();
        personaRepository.save(adminPersona);

        Usuario admin = new Usuario();
        admin.setNombre(ADMIN_USER);
        admin.setContrasenia(md5(ADMIN_PASS_PLAIN));
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

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 no disponible en la JVM", e);
        }
    }
}
