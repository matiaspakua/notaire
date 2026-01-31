package com.licensis.notaire.config;

import com.licensis.notaire.jpa.PersonaJpaController;
import com.licensis.notaire.jpa.TipoIdentificacionJpaController;
import com.licensis.notaire.jpa.UsuarioJpaController;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.negocio.Usuario;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Crea usuario admin/persona/tipo identificación inicial si la base está vacía (p. ej. con ddl-auto=create).
 */
@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS_PLAIN = "admin";

    @PostConstruct
    public void init() {
        try {
            UsuarioJpaController usuarioJpa = UsuarioJpaController.getInstancia();
            List<Usuario> usuarios = usuarioJpa.buscarUsuarios();
            if (usuarios != null && !usuarios.isEmpty()) {
                log.info("Usuarios ya existen, omitiendo creación de admin.");
                return;
            }
            var emf = JpaControllerProvider.getEntityManagerFactory();
            TipoIdentificacionJpaController tipoIdJpa = TipoIdentificacionJpaController.getInstancia();
            List<TipoIdentificacion> tipos = tipoIdJpa.findTipoIdentificacionEntities();
            TipoIdentificacion tipoDni;
            if (tipos != null && !tipos.isEmpty()) {
                tipoDni = tipos.get(0);
            } else {
                tipoDni = new TipoIdentificacion();
                tipoDni.setNombre("DNI");
                tipoIdJpa.create(tipoDni);
            }

            PersonaJpaController personaJpa = PersonaJpaController.getInstancia();
            Persona adminPersona = new Persona();
            adminPersona.setNombre("Admin");
            adminPersona.setApellido("Sistema");
            adminPersona.setEsCliente(false);
            adminPersona.setNumeroIdentificacion("00000000");
            adminPersona.setFkIdTipoIdentificacion(tipoDni);
            personaJpa.create(adminPersona);

            Usuario admin = new Usuario();
            admin.setNombre(ADMIN_USER);
            admin.setContrasenia(md5(ADMIN_PASS_PLAIN));
            admin.setEstado(true);
            admin.setTipo("Escribano");
            admin.setFkIdPersona(adminPersona);
            usuarioJpa.create(admin);
            log.info("Usuario inicial 'admin' creado (contraseña: admin).");
        } catch (Exception e) {
            log.warn("No se pudo crear usuario inicial: {}", e.getMessage());
        }
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
            throw new RuntimeException(e);
        }
    }
}
