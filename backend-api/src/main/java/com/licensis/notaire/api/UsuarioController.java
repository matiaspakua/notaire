package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.jpa.UsuarioJpaController;
import com.licensis.notaire.negocio.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "API para gestionar usuarios")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private UsuarioJpaController getJpaController() {
        return UsuarioJpaController.getInstancia();
    }

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios")
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        try {
            List<Usuario> usuarios = getJpaController().buscarUsuarios();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/persona/{idPersona}")
    @Operation(summary = "Obtener usuario por id de persona asociada (Batch A)")
    public ResponseEntity<Usuario> getUsuarioByPersona(@PathVariable Integer idPersona) {
        try {
            Usuario usuario = getJpaController().findUsuarioByPersona(idPersona);
            return usuario != null ? ResponseEntity.ok(usuario) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Integer id) {
        try {
            Usuario usuario = getJpaController().findUsuarios(id);
            if (usuario != null) {
                return ResponseEntity.ok(usuario);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario")
    public ResponseEntity<Void> createUsuario(@RequestBody Usuario usuario) {
        try {
            // Encriptar contraseña antes de guardar
            if (usuario.getContrasenia() != null && !usuario.getContrasenia().isEmpty()) {
                String encryptedPassword = encriptaEnMD5(usuario.getContrasenia());
                usuario.setContrasenia(encryptedPassword);
            }
            getJpaController().create(usuario);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<Void> updateUsuario(@PathVariable Integer id, @RequestBody Usuario usuario) {
        try {
            usuario.setIdUsuario(id);
            // Encriptar contraseña si se está actualizando
            if (usuario.getContrasenia() != null && !usuario.getContrasenia().isEmpty()) {
                String encryptedPassword = encriptaEnMD5(usuario.getContrasenia());
                usuario.setContrasenia(encryptedPassword);
            }
            getJpaController().edit(usuario);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario")
    public ResponseEntity<DtoUsuario> login(@RequestBody DtoUsuario loginRequest) {
        try {
            List<Usuario> usuarios = getJpaController().buscarUsuarios();

            if (usuarios == null || usuarios.isEmpty()) {
                log.warn("Login fallido: No hay usuarios en la base de datos.");
                DtoUsuario errorResponse = new DtoUsuario();
                errorResponse.setValido(false);
                return ResponseEntity.ok(errorResponse);
            }

            String passwordIngresado = encriptaEnMD5(loginRequest.getContrasenia());
            log.debug("Intento de login para usuario: '{}' (hash: '{}')", loginRequest.getNombre(), passwordIngresado);

            for (Usuario usuario : usuarios) {
                log.debug("Comparando con usuario en DB: '{}' (estado: {}, hash_db: '{}')",
                        usuario.getNombre(), usuario.getEstado(), usuario.getContrasenia());
                if (usuario.getNombre().equalsIgnoreCase(loginRequest.getNombre())) {
                    if (usuario.getContrasenia().equals(passwordIngresado)) {
                        if (usuario.getEstado()) {
                            log.info("Login exitoso para usuario: '{}'", usuario.getNombre());
                            DtoUsuario dtoUsuario = usuario.getDto();
                            dtoUsuario.setValido(true);
                            return ResponseEntity.ok(dtoUsuario);
                        } else {
                            log.warn("Login fallido para '{}': usuario inactivo", usuario.getNombre());
                        }
                    } else {
                        log.warn("Login fallido para '{}': contraseña incorrecta", usuario.getNombre());
                    }
                }
            }

            log.warn("Login fallido: usuario '{}' no encontrado en {} usuarios cargados.", loginRequest.getNombre(),
                    usuarios.size());
            DtoUsuario errorResponse = new DtoUsuario();
            errorResponse.setValido(false);
            return ResponseEntity.ok(errorResponse);

        } catch (Exception e) {
            DtoUsuario errorResponse = new DtoUsuario();
            errorResponse.setValido(false);
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Encripta una cadena usando MD5
     */
    private String encriptaEnMD5(String stringAEncriptar) {
        char[] CONSTS_HEX = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
        try {
            MessageDigest msgd = MessageDigest.getInstance("MD5");
            byte[] bytes = msgd.digest(stringAEncriptar.getBytes());
            StringBuilder strbCadenaMD5 = new StringBuilder(2 * bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                int bajo = (int) (bytes[i] & 0x0f);
                int alto = (int) ((bytes[i] & 0xf0) >> 4);
                strbCadenaMD5.append(CONSTS_HEX[alto]);
                strbCadenaMD5.append(CONSTS_HEX[bajo]);
            }
            return strbCadenaMD5.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
