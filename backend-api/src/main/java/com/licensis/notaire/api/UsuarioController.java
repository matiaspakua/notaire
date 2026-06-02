package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "API para gestionar usuarios")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios")
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/persona/{idPersona}")
    @Operation(summary = "Obtener usuario por id de persona asociada")
    public ResponseEntity<Usuario> getUsuarioByPersona(@PathVariable Integer idPersona) {
        return usuarioRepository.findFirstByFkIdPersonaIdPersona(idPersona)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Integer id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario")
    public ResponseEntity<Object> createUsuario(@RequestBody Usuario usuario) {
        try {
            if (usuario.getContrasenia() != null && !usuario.getContrasenia().isEmpty()) {
                usuario.setContrasenia(encriptaEnMD5(usuario.getContrasenia()));
            }
            usuario = usuarioRepository.save(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
        } catch (Exception e) {
            log.error("Failed to create usuario", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<Void> updateUsuario(@PathVariable Integer id, @RequestBody Usuario usuario) {
        Optional<Usuario> existing = usuarioRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            usuario.setIdUsuario(id);
            if (usuario.getContrasenia() != null && !usuario.getContrasenia().isEmpty()) {
                usuario.setContrasenia(encriptaEnMD5(usuario.getContrasenia()));
            } else {
                // Preserve the stored password when the update omits it
                // (contrasenia is NOT NULL — a null would violate the constraint).
                usuario.setContrasenia(existing.get().getContrasenia());
            }
            usuarioRepository.save(usuario);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update usuario id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Integer id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            usuarioRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete usuario id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario")
    public ResponseEntity<?> login(@RequestBody DtoUsuario loginRequest) {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();

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
                            DtoUsuario dtoUsuario = new DtoUsuario();
                            dtoUsuario.setIdUsuario(usuario.getIdUsuario());
                            dtoUsuario.setNombre(usuario.getNombre());
                            dtoUsuario.setEstado(usuario.getEstado());
                            dtoUsuario.setTipo(usuario.getTipo());
                            dtoUsuario.setVersion(usuario.getVersion());
                            if (usuario.getFkIdPersona() != null) {
                                DtoPersona dtoPersona = new DtoPersona();
                                dtoPersona.setIdPersona(usuario.getFkIdPersona().getIdPersona());
                                dtoPersona.setNombre(usuario.getFkIdPersona().getNombre());
                                dtoPersona.setApellido(usuario.getFkIdPersona().getApellido());
                                dtoUsuario.setPersonas(dtoPersona);
                            }
                            dtoUsuario.setValido(true);
                            log.debug("DTO Usuario creado - valido: {}, estado: {}", dtoUsuario.isValido(), dtoUsuario.isEstado());
                            
                            // Create a map response to ensure 'valido' field is included
                            Map<String, Object> response = new HashMap<>();
                            response.put("valido", true);
                            response.put("idUsuario", dtoUsuario.getIdUsuario());
                            response.put("nombre", dtoUsuario.getNombre());
                            response.put("estado", dtoUsuario.isEstado());
                            response.put("tipo", dtoUsuario.getTipo());
                            response.put("version", dtoUsuario.getVersion());
                            if (dtoUsuario.getPersonas() != null) {
                                Map<String, Object> personaMap = new HashMap<>();
                                personaMap.put("idPersona", dtoUsuario.getPersonas().getIdPersona());
                                personaMap.put("nombre", dtoUsuario.getPersonas().getNombre());
                                personaMap.put("apellido", dtoUsuario.getPersonas().getApellido());
                                response.put("personas", personaMap);
                            }
                            return ResponseEntity.ok(response);
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
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valido", false);
            return ResponseEntity.ok(errorResponse);

        } catch (Exception e) {
            log.error("Failed to process login for {}", loginRequest.getNombre(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valido", false);
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
