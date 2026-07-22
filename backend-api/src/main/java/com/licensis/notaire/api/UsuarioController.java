package com.licensis.notaire.api;

import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.observability.MetricsUtil;
import com.licensis.notaire.repository.UsuarioRepository;
import com.licensis.notaire.security.LoginAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

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
    private final JwtTokenService jwtTokenService;
    private final MetricsUtil metricsUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    public UsuarioController(UsuarioRepository usuarioRepository, JwtTokenService jwtTokenService,
                             MetricsUtil metricsUtil, PasswordEncoder passwordEncoder,
                             LoginAttemptService loginAttemptService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtTokenService = jwtTokenService;
        this.metricsUtil = metricsUtil;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
    }

    record PersonaInfo(Integer idPersona, String nombre, String apellido) {}

    record RolInfo(Integer idRol, String nombre) {}

    record UsuarioResponse(Integer idUsuario, String nombre, String tipo, boolean activo,
                            PersonaInfo persona, RolInfo rol) {}

    record UsuarioRequest(String nombre, String contrasenia, String tipo, boolean activo) {}

    private UsuarioResponse toResponse(Usuario u) {
        PersonaInfo persona = null;
        if (u.getFkIdPersona() != null) {
            var p = u.getFkIdPersona();
            persona = new PersonaInfo(p.getIdPersona(), p.getNombre(), p.getApellido());
        }
        RolInfo rolInfo = null;
        if (u.getRol() != null) {
            rolInfo = new RolInfo(u.getRol().getIdRol(), u.getRol().getNombre());
        }
        return new UsuarioResponse(u.getIdUsuario(), u.getNombre(), u.getTipo(), u.getEstado(), persona, rolInfo);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UsuarioResponse>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/persona/{idPersona}")
    @Operation(summary = "Obtener usuario por id de persona asociada")
    @Transactional(readOnly = true)
    public ResponseEntity<UsuarioResponse> getUsuarioByPersona(@PathVariable Integer idPersona) {
        return usuarioRepository.findFirstByFkIdPersonaIdPersona(idPersona)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<UsuarioResponse> getUsuarioById(@PathVariable Integer id) {
        return usuarioRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo usuario")
    public ResponseEntity<Object> createUsuario(@RequestBody UsuarioRequest request) {
        try {
            Usuario usuario = new Usuario();
            usuario.setNombre(request.nombre());
            usuario.setTipo(request.tipo());
            usuario.setEstado(request.activo());
            String pwd = request.contrasenia();
            usuario.setContrasenia(pwd != null && !pwd.isEmpty() ? passwordEncoder.encode(pwd) : "");
            usuario = usuarioRepository.save(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(usuario));
        } catch (Exception e) {
            log.error("Failed to create usuario", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<Void> updateUsuario(@PathVariable Integer id, @RequestBody UsuarioRequest request) {
        Optional<Usuario> existing = usuarioRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            Usuario usuario = existing.get();
            usuario.setNombre(request.nombre());
            usuario.setTipo(request.tipo());
            usuario.setEstado(request.activo());
            String pwd = request.contrasenia();
            if (pwd != null && !pwd.isEmpty()) {
                usuario.setContrasenia(passwordEncoder.encode(pwd));
            }
            usuarioRepository.save(usuario);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update usuario id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
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

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK (ver campo 'valido' para el resultado)"),
    @ApiResponse(responseCode = "429", description = "Cuenta bloqueada temporalmente por intentos fallidos")
})
    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario")
    public ResponseEntity<?> login(@RequestBody DtoUsuario loginRequest) {
        try {
            if (loginAttemptService.isLocked(loginRequest.getNombre())) {
                log.warn("Login bloqueado para '{}': demasiados intentos fallidos.", loginRequest.getNombre());
                metricsUtil.incrementCounter("login", "locked");
                Map<String, Object> lockedResponse = new HashMap<>();
                lockedResponse.put("valido", false);
                lockedResponse.put("message", "Cuenta bloqueada temporalmente por demasiados intentos fallidos.");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(lockedResponse);
            }

            List<Usuario> usuarios = usuarioRepository.findAll();

            if (usuarios == null || usuarios.isEmpty()) {
                log.warn("Login fallido: No hay usuarios en la base de datos.");
                DtoUsuario errorResponse = new DtoUsuario();
                errorResponse.setValido(false);
                return ResponseEntity.ok(errorResponse);
            }

            log.debug("Intento de login para usuario: '{}'", loginRequest.getNombre());

            for (Usuario usuario : usuarios) {
                if (usuario.getNombre().equalsIgnoreCase(loginRequest.getNombre())) {
                    if (passwordMatches(loginRequest.getContrasenia(), usuario)) {
                        if (usuario.getEstado()) {
                            log.info("Login exitoso para usuario: '{}'", usuario.getNombre());
                            metricsUtil.incrementCounter("login", "success");
                            loginAttemptService.onLoginSucceeded(usuario.getNombre());
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
                            response.put("token", jwtTokenService.generateToken(usuario.getNombre()));
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
                            metricsUtil.incrementCounter("login", "inactive");
                            loginAttemptService.onLoginFailed(usuario.getNombre());
                            return ResponseEntity.ok(invalidLoginResponse());
                        }
                    } else {
                        log.warn("Login fallido para '{}': contraseña incorrecta", usuario.getNombre());
                        metricsUtil.incrementCounter("login", "bad_credentials");
                        loginAttemptService.onLoginFailed(usuario.getNombre());
                        return ResponseEntity.ok(invalidLoginResponse());
                    }
                }
            }

            log.warn("Login fallido: usuario '{}' no encontrado en {} usuarios cargados.", loginRequest.getNombre(),
                    usuarios.size());
            metricsUtil.incrementCounter("login", "not_found");
            loginAttemptService.onLoginFailed(loginRequest.getNombre());
            return ResponseEntity.ok(invalidLoginResponse());

        } catch (Exception e) {
            log.error("Failed to process login for {}", loginRequest.getNombre(), e);
            metricsUtil.incrementCounter("login", "error");
            return ResponseEntity.ok(invalidLoginResponse());
        }
    }

    private Map<String, Object> invalidLoginResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("valido", false);
        return response;
    }

    /**
     * Verifica la contraseña ingresada contra el hash almacenado, soportando tanto el
     * nuevo formato BCrypt como el legado MD5 (issue #554). Si el hash almacenado
     * todavía es MD5 y la contraseña coincide, se re-encripta en BCrypt y se persiste
     * de forma transparente, migrando la credencial sin exigir un reseteo al usuario.
     */
    private boolean passwordMatches(String rawPassword, Usuario usuario) {
        String storedHash = usuario.getContrasenia();
        if (isBcryptHash(storedHash)) {
            return passwordEncoder.matches(rawPassword, storedHash);
        }
        if (!legacyMd5Hash(rawPassword).equals(storedHash)) {
            return false;
        }
        usuario.setContrasenia(passwordEncoder.encode(rawPassword));
        usuarioRepository.save(usuario);
        return true;
    }

    private boolean isBcryptHash(String hash) {
        return hash != null && hash.startsWith("$2");
    }

    /**
     * Hash MD5 legado, mantenido únicamente para verificar credenciales sembradas
     * antes de la migración a BCrypt (issue #554); no se usa para generar hashes nuevos.
     */
    private String legacyMd5Hash(String stringAEncriptar) {
        char[] hexChars = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
        try {
            MessageDigest msgd = MessageDigest.getInstance("MD5");
            byte[] bytes = msgd.digest(stringAEncriptar.getBytes());
            StringBuilder strbCadenaMD5 = new StringBuilder(2 * bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                int bajo = (int) (bytes[i] & 0x0f);
                int alto = (int) ((bytes[i] & 0xf0) >> 4);
                strbCadenaMD5.append(hexChars[alto]);
                strbCadenaMD5.append(hexChars[bajo]);
            }
            return strbCadenaMD5.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
