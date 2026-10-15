package com.licensis.notaire.adapter.in.web.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.licensis.notaire.application.port.in.user.AuthenticateUserUseCase;
import com.licensis.notaire.application.port.in.user.AuthenticateUserUseCase.Result;
import com.licensis.notaire.application.port.in.user.ManageUsersUseCase;
import com.licensis.notaire.application.port.in.user.ManageUsersUseCase.CreateUserCommand;
import com.licensis.notaire.application.port.in.user.ManageUsersUseCase.UpdateUserCommand;
import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.config.AuthCookieService;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.config.RequestTokenResolver;
import com.licensis.notaire.config.TokenRevocationService;
import com.licensis.notaire.domain.user.User;
import com.licensis.notaire.domain.user.UserNotFoundException;
import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.observability.MetricsUtil;
import com.licensis.notaire.security.LoginAttemptService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Users. Calls inbound ports only; JWT/cookies/metrics/lockout stay in this adapter (issue #1731).
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "API for managing users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final QueryUsersUseCase query;
    private final ManageUsersUseCase manage;
    private final AuthenticateUserUseCase authenticate;
    private final JwtTokenService jwtTokenService;
    private final MetricsUtil metricsUtil;
    private final LoginAttemptService loginAttemptService;
    private final AuthCookieService authCookieService;
    private final TokenRevocationService tokenRevocationService;

    public UserController(
            QueryUsersUseCase query,
            ManageUsersUseCase manage,
            AuthenticateUserUseCase authenticate,
            JwtTokenService jwtTokenService,
            MetricsUtil metricsUtil,
            LoginAttemptService loginAttemptService,
            AuthCookieService authCookieService,
            TokenRevocationService tokenRevocationService) {
        this.query = query;
        this.manage = manage;
        this.authenticate = authenticate;
        this.jwtTokenService = jwtTokenService;
        this.metricsUtil = metricsUtil;
        this.loginAttemptService = loginAttemptService;
        this.authCookieService = authCookieService;
        this.tokenRevocationService = tokenRevocationService;
    }

    record PersonInfo(Integer idPerson, String name, String lastName) {}

    record RoleInfo(Integer idRole, String name) {}

    record UserResponse(
            Integer idUser, String name, String type, boolean active, PersonInfo person, RoleInfo role) {}

    record UserRequest(@NotBlank String name, String password, @NotBlank String type, boolean active) {}

    private UserResponse toResponse(User u) {
        PersonInfo person = null;
        if (u.personId() != null) {
            person = new PersonInfo(u.personId(), u.personFirstName(), u.personLastName());
        }
        RoleInfo roleInfo = null;
        if (u.roleId() != null) {
            roleInfo = new RoleInfo(u.roleId(), u.roleName());
        }
        return new UserResponse(u.id(), u.name(), u.type(), u.active(), person, roleInfo);
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(query.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/person/{idPerson}")
    @Operation(summary = "Get user by associated person id")
    public ResponseEntity<UserResponse> getUserByPerson(@PathVariable Integer idPerson) {
        return query.findByPersonId(idPerson)
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
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        return query.findById(id)
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
            User saved = manage.create(
                    new CreateUserCommand(request.name(), request.password(), request.type(), request.active()));
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
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
        try {
            manage.update(
                    id, new UpdateUserCommand(request.name(), request.password(), request.type(), request.active()));
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
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
        try {
            manage.delete(id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
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
    public ResponseEntity<?> login(@RequestBody DtoUser loginRequest, HttpServletResponse httpResponse) {
        try {
            if (loginAttemptService.isLocked(loginRequest.getName())) {
                log.warn("Login locked for '{}': too many failed attempts.", loginRequest.getName());
                metricsUtil.incrementCounter("login", "locked");
                Map<String, Object> lockedResponse = new HashMap<>();
                lockedResponse.put("valid", false);
                lockedResponse.put("message", "Account temporarily locked after too many failed attempts.");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(lockedResponse);
            }

            Result result = authenticate.authenticate(loginRequest.getName(), loginRequest.getPassword());
            if (result instanceof Result.Failed failed) {
                return handleAuthFailure(failed.failure(), loginRequest.getName());
            }
            User user = ((Result.Success) result).user();
            log.info("Login succeeded for user: '{}'", user.name());
            metricsUtil.incrementCounter("login", "success");
            loginAttemptService.onLoginSucceeded(user.name());

            String jwt = jwtTokenService.generateToken(user.name());
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            // Bearer token kept for Bruno/API clients; browsers use the HttpOnly cookie (#1051).
            response.put("token", jwt);
            response.put("idUser", user.id());
            response.put("name", user.name());
            response.put("status", user.active());
            response.put("type", user.type());
            response.put("version", user.version());
            if (user.personId() != null) {
                Map<String, Object> personMap = new HashMap<>();
                personMap.put("idPerson", user.personId());
                personMap.put("firstName", user.personFirstName());
                personMap.put("lastName", user.personLastName());
                response.put("person", personMap);
            }
            ResponseCookie sessionCookie = authCookieService.createSessionCookie(jwt);
            // addHeader, not the ResponseEntity header map: entity headers are
            // applied with set semantics and would drop the XSRF-TOKEN cookie
            // written by the security chain (issue #1620).
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, sessionCookie.toString());
            return ResponseEntity.ok().body(response);
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
    @Operation(
            summary = "Sign out (revokes the presented token and clears the HttpOnly cookie)",
            description = "Revokes on the server the JWT sent by Bearer or cookie, so it cannot "
                    + "be reused even if it has not expired; the user's other sessions remain active. "
                    + "A missing or invalid token gets the same response (issue #676).")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse httpResponse) {
        tokenRevocationService.revoke(RequestTokenResolver.resolve(request, authCookieService.cookieName()));
        ResponseCookie cleared = authCookieService.clearSessionCookie();
        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        // Same add-header rule as login: the entity header map would replace the
        // XSRF-TOKEN cookie set by the security chain (issue #1620).
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cleared.toString());
        return ResponseEntity.ok().body(body);
    }

    private ResponseEntity<?> handleAuthFailure(AuthenticateUserUseCase.Failure failure, String username) {
        switch (failure) {
            case NOT_FOUND -> {
                log.warn("Login failed: user '{}' not found.", username);
                metricsUtil.incrementCounter("login", "not_found");
            }
            case BAD_CREDENTIALS -> {
                log.warn("Login failed for '{}': incorrect password", username);
                metricsUtil.incrementCounter("login", "bad_credentials");
            }
            case INACTIVE -> {
                log.warn("Login failed for '{}': inactive user", username);
                metricsUtil.incrementCounter("login", "inactive");
            }
            default -> {
                // sealed enum — all cases covered
            }
        }
        loginAttemptService.onLoginFailed(username);
        return ResponseEntity.ok(invalidLoginResponse());
    }

    private Map<String, Object> invalidLoginResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("valid", false);
        return response;
    }
}
