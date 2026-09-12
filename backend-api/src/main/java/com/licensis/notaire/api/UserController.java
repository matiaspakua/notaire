package com.licensis.notaire.api;

import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.business.User;
import com.licensis.notaire.observability.MetricsUtil;
import com.licensis.notaire.repository.UserRepository;
import com.licensis.notaire.security.LoginAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final MetricsUtil metricsUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    public UserController(UserRepository userRepository, JwtTokenService jwtTokenService,
                             MetricsUtil metricsUtil, PasswordEncoder passwordEncoder,
                             LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.metricsUtil = metricsUtil;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
    }

    record PersonInfo(Integer idPerson, String name, String lastName) {}

    record RoleInfo(Integer idRole, String name) {}

    record UserResponse(Integer idUser, String name, String type, boolean active,
                            PersonInfo person, RoleInfo role) {}

    record UserRequest(
            @NotBlank String name,
            String password,
            @NotBlank String type,
            boolean active) {}

    private UserResponse toResponse(User u) {
        PersonInfo person = null;
        if (u.getFkIdPerson() != null) {
            var p = u.getFkIdPerson();
            person = new PersonInfo(p.getPersonId(), p.getFirstName(), p.getLastName());
        }
        RoleInfo roleInfo = null;
        if (u.getRole() != null) {
            roleInfo = new RoleInfo(u.getRole().getIdRole(), u.getRole().getName());
        }
        return new UserResponse(u.getIdUser(), u.getName(), u.getType(), u.getStatus(), person, roleInfo);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/persona/{idPerson}")
    @Operation(summary = "Obtener usuario por id de persona asociada")
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponse> getUserByPerson(@PathVariable Integer idPerson) {
        return userRepository.findFirstByFkIdPersonIdPerson(idPerson)
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
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        return userRepository.findById(id)
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
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserRequest request) {
        try {
            User user = new User();
            user.setName(request.name());
            user.setType(request.type());
            user.setStatus(request.active());
            String pwd = request.password();
            user.setPassword(pwd != null && !pwd.isEmpty() ? passwordEncoder.encode(pwd) : "");
            user = userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
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
    public ResponseEntity<Void> updateUser(@PathVariable Integer id, @Valid @RequestBody UserRequest request) {
        Optional<User> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            User user = existing.get();
            user.setName(request.name());
            user.setType(request.type());
            user.setStatus(request.active());
            String pwd = request.password();
            if (pwd != null && !pwd.isEmpty()) {
                user.setPassword(passwordEncoder.encode(pwd));
            }
            userRepository.save(user);
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
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            userRepository.deleteById(id);
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
    public ResponseEntity<?> login(@RequestBody DtoUser loginRequest) {
        try {
            if (loginAttemptService.isLocked(loginRequest.getName())) {
                log.warn("Login bloqueado para '{}': demasiados intentos fallidos.", loginRequest.getName());
                metricsUtil.incrementCounter("login", "locked");
                Map<String, Object> lockedResponse = new HashMap<>();
                lockedResponse.put("valido", false);
                lockedResponse.put("message", "Cuenta bloqueada temporalmente por demasiados intentos fallidos.");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(lockedResponse);
            }

            List<User> users = userRepository.findAll();

            if (users == null || users.isEmpty()) {
                log.warn("Login fallido: No hay usuarios en la base de datos.");
                DtoUser errorResponse = new DtoUser();
                errorResponse.setValido(false);
                return ResponseEntity.ok(errorResponse);
            }

            log.debug("Intento de login para usuario: '{}'", loginRequest.getName());

            for (User user : users) {
                if (user.getName().equalsIgnoreCase(loginRequest.getName())) {
                    if (passwordMatches(loginRequest.getPassword(), user)) {
                        if (user.getStatus()) {
                            log.info("Login exitoso para usuario: '{}'", user.getName());
                            metricsUtil.incrementCounter("login", "success");
                            loginAttemptService.onLoginSucceeded(user.getName());
                            DtoUser dtoUser = new DtoUser();
                            dtoUser.setIdUser(user.getIdUser());
                            dtoUser.setName(user.getName());
                            dtoUser.setStatus(user.getStatus());
                            dtoUser.setType(user.getType());
                            dtoUser.setVersion(user.getVersion());
                            if (user.getFkIdPerson() != null) {
                                DtoPerson dtoPerson = new DtoPerson();
                                dtoPerson.setId(user.getFkIdPerson().getPersonId());
                                dtoPerson.setFirstName(user.getFkIdPerson().getFirstName());
                                dtoPerson.setLastName(user.getFkIdPerson().getLastName());
                                dtoUser.setPersons(dtoPerson);
                            }
                            dtoUser.setValido(true);
                            log.debug("DTO Usuario creado - valido: {}, estado: {}", dtoUser.isValido(), dtoUser.isStatus());

                            // Create a map response to ensure 'valido' field is included
                            Map<String, Object> response = new HashMap<>();
                            response.put("valido", true);
                            response.put("token", jwtTokenService.generateToken(user.getName()));
                            response.put("idUsuario", dtoUser.getIdUser());
                            response.put("nombre", dtoUser.getName());
                            response.put("estado", dtoUser.isStatus());
                            response.put("tipo", dtoUser.getType());
                            response.put("version", dtoUser.getVersion());
                            if (dtoUser.getPersons() != null) {
                                Map<String, Object> personMap = new HashMap<>();
                                personMap.put("idPersona", dtoUser.getPersons().getId());
                                personMap.put("nombre", dtoUser.getPersons().getFirstName());
                                personMap.put("apellido", dtoUser.getPersons().getLastName());
                                response.put("personas", personMap);
                            }
                            return ResponseEntity.ok(response);
                        } else {
                            log.warn("Login fallido para '{}': usuario inactivo", user.getName());
                            metricsUtil.incrementCounter("login", "inactive");
                            loginAttemptService.onLoginFailed(user.getName());
                            return ResponseEntity.ok(invalidLoginResponse());
                        }
                    } else {
                        log.warn("Login fallido para '{}': contraseña incorrecta", user.getName());
                        metricsUtil.incrementCounter("login", "bad_credentials");
                        loginAttemptService.onLoginFailed(user.getName());
                        return ResponseEntity.ok(invalidLoginResponse());
                    }
                }
            }

            log.warn("Login fallido: usuario '{}' no encontrado en {} usuarios cargados.", loginRequest.getName(),
                    users.size());
            metricsUtil.incrementCounter("login", "not_found");
            loginAttemptService.onLoginFailed(loginRequest.getName());
            return ResponseEntity.ok(invalidLoginResponse());

        } catch (Exception e) {
            log.error("Failed to process login for {}", loginRequest.getName(), e);
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
    private boolean passwordMatches(String rawPassword, User user) {
        String storedHash = user.getPassword();
        if (isBcryptHash(storedHash)) {
            return passwordEncoder.matches(rawPassword, storedHash);
        }
        if (!legacyMd5Hash(rawPassword).equals(storedHash)) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
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
