package com.licensis.notaire.adapter.in.web.user;

import com.licensis.notaire.config.AuthCookieService;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.config.RequestTokenResolver;
import com.licensis.notaire.config.TokenRevocationService;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "API for managing users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final MetricsUtil metricsUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final AuthCookieService authCookieService;
    private final TokenRevocationService tokenRevocationService;

    public UserController(UserRepository userRepository, JwtTokenService jwtTokenService,
                             MetricsUtil metricsUtil, PasswordEncoder passwordEncoder,
                             LoginAttemptService loginAttemptService,
                             AuthCookieService authCookieService,
                             TokenRevocationService tokenRevocationService) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.metricsUtil = metricsUtil;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.authCookieService = authCookieService;
        this.tokenRevocationService = tokenRevocationService;
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
    @Operation(summary = "Get all users")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/person/{idPerson}")
    @Operation(summary = "Get user by associated person id")
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponse> getUserByPerson(@PathVariable Integer idPerson) {
        return userRepository.findFirstByFkIdPersonIdPerson(idPerson)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "409", description = "Conflict")
})
    @PostMapping
    @Operation(summary = "Create user")
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
            log.error("Failed to create user", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @PutMapping("/{id}")
    @Operation(summary = "Update user")
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
            log.error("Failed to update user id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Deleted"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete user id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK (see the 'valid' field for the result)"),
    @ApiResponse(responseCode = "429", description = "Account temporarily locked after failed attempts")
})
    @PostMapping("/login")
    @Operation(summary = "Authenticate user")
    public ResponseEntity<?> login(@RequestBody DtoUser loginRequest) {
        try {
            if (loginAttemptService.isLocked(loginRequest.getName())) {
                log.warn("Login locked for '{}': too many failed attempts.", loginRequest.getName());
                metricsUtil.incrementCounter("login", "locked");
                Map<String, Object> lockedResponse = new HashMap<>();
                lockedResponse.put("valid", false);
                lockedResponse.put("message", "Account temporarily locked after too many failed attempts.");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(lockedResponse);
            }

            List<User> users = userRepository.findAll();

            if (users == null || users.isEmpty()) {
                log.warn("Login failed: There are no users in the database.");
                DtoUser errorResponse = new DtoUser();
                errorResponse.setValid(false);
                return ResponseEntity.ok(errorResponse);
            }

            log.debug("Login attempt for user: '{}'", loginRequest.getName());

            for (User user : users) {
                if (user.getName().equalsIgnoreCase(loginRequest.getName())) {
                    if (passwordMatches(loginRequest.getPassword(), user)) {
                        if (user.getStatus()) {
                            log.info("Login succeeded for user: '{}'", user.getName());
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
                            dtoUser.setValid(true);
                            log.debug("DTO UserAccount created - valid: {}, status: {}", dtoUser.isValid(), dtoUser.isStatus());

                            // Create a map response to ensure 'valid' field is included
                            String jwt = jwtTokenService.generateToken(user.getName());
                            Map<String, Object> response = new HashMap<>();
                            response.put("valid", true);
                            // Bearer token kept for Bruno/API clients; browsers use the HttpOnly cookie (#1051).
                            response.put("token", jwt);
                            response.put("idUser", dtoUser.getIdUser());
                            response.put("name", dtoUser.getName());
                            response.put("status", dtoUser.isStatus());
                            response.put("type", dtoUser.getType());
                            response.put("version", dtoUser.getVersion());
                            if (dtoUser.getPersons() != null) {
                                Map<String, Object> personMap = new HashMap<>();
                                personMap.put("idPerson", dtoUser.getPersons().getId());
                                personMap.put("firstName", dtoUser.getPersons().getFirstName());
                                personMap.put("lastName", dtoUser.getPersons().getLastName());
                                response.put("person", personMap);
                            }
                            ResponseCookie sessionCookie = authCookieService.createSessionCookie(jwt);
                            return ResponseEntity.ok()
                                    .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                                    .body(response);
                        } else {
                            log.warn("Login failed for '{}': inactive user", user.getName());
                            metricsUtil.incrementCounter("login", "inactive");
                            loginAttemptService.onLoginFailed(user.getName());
                            return ResponseEntity.ok(invalidLoginResponse());
                        }
                    } else {
                        log.warn("Login failed for '{}': incorrect password", user.getName());
                        metricsUtil.incrementCounter("login", "bad_credentials");
                        loginAttemptService.onLoginFailed(user.getName());
                        return ResponseEntity.ok(invalidLoginResponse());
                    }
                }
            }

            log.warn("Login failed: user '{}' not found among {} loaded users.", loginRequest.getName(),
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

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token revoked (if any) and auth cookie cleared")
    })
    @PostMapping("/logout")
    @Operation(summary = "Sign out (revokes the presented token and clears the HttpOnly cookie)",
            description = "Revokes on the server the JWT sent by Bearer or cookie, so it cannot "
                    + "be reused even if it has not expired; the user's other sessions remain active. "
                    + "A missing or invalid token gets the same response (issue #676).")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        tokenRevocationService.revoke(RequestTokenResolver.resolve(request, authCookieService.cookieName()));
        ResponseCookie cleared = authCookieService.clearSessionCookie();
        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleared.toString())
                .body(body);
    }

    private Map<String, Object> invalidLoginResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("valid", false);
        return response;
    }

    /**
     * Checks the submitted password against the stored hash. Accepts BCrypt and
     * the legacy MD5 form (issue #554). When the stored hash is still MD5 and the
     * password matches, it re-encodes the password as BCrypt and saves it, so the
     * credential migrates without a reset.
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
     * Legacy MD5 hash, kept only to check credentials seeded before the BCrypt
     * migration (issue #554). It is not used to create new hashes.
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
