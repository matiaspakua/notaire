package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.licensis.notaire.adapter.in.web.user.UserController;
import com.licensis.notaire.adapter.out.persistence.user.PasswordHasherAdapter;
import com.licensis.notaire.application.port.out.user.UserCatalogPort;
import com.licensis.notaire.application.usecase.user.AuthenticateUserService;
import com.licensis.notaire.application.usecase.user.UserManagementService;
import com.licensis.notaire.application.usecase.user.UserQueryService;
import com.licensis.notaire.config.AuthCookieService;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.config.TokenRevocationService;
import com.licensis.notaire.domain.user.User;
import com.licensis.notaire.observability.MetricsUtil;
import com.licensis.notaire.security.InMemoryLoginAttemptStore;
import com.licensis.notaire.security.LoginAttemptService;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController — password hashing (issue #554 / #1731)")
class UserControllerHashTest {

    private static final String LEGACY_MD5_ADMIN = "21232f297a57a5a743894a0e4a801fc3";
    private static final String BCRYPT_HASH = "$2a$12$stub.bcrypt.hash.value.................";

    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private MetricsUtil metricsUtil;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthCookieService authCookieService;
    @Mock
    private TokenRevocationService tokenRevocationService;

    private InMemoryUserCatalog catalog;
    private UserController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        catalog = new InMemoryUserCatalog();
        PasswordHasherAdapter hasher = new PasswordHasherAdapter(passwordEncoder);
        UserManagementService manage = new UserManagementService(catalog, hasher);
        AuthenticateUserService authenticate = new AuthenticateUserService(catalog, hasher);
        UserQueryService query = new UserQueryService(catalog);
        lenient()
                .when(authCookieService.createSessionCookie(anyString()))
                .thenReturn(org.springframework.http.ResponseCookie.from("notaire-auth-token", "x")
                        .httpOnly(true)
                        .path("/")
                        .sameSite("Lax")
                        .build());
        controller = new UserController(
                query,
                manage,
                authenticate,
                jwtTokenService,
                metricsUtil,
                new LoginAttemptService(5, 900000, new InMemoryLoginAttemptStore()),
                authCookieService,
                tokenRevocationService);
        mockMvc = standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("createUser should store a BCrypt-encoded hash, not plaintext or MD5")
    void createUserShouldStoreBcryptHash() throws Exception {
        when(passwordEncoder.encode("secret")).thenReturn(BCRYPT_HASH);

        mockMvc.perform(post("/api/v1/users")
                .contentType("application/json")
                .content("{\"name\":\"juan\",\"password\":\"secret\",\"type\":\"Escribano\",\"active\":true}"));

        assertThat(catalog.findAll()).hasSize(1);
        assertThat(catalog.findAll().get(0).passwordHash()).isEqualTo(BCRYPT_HASH);
    }

    @Test
    @DisplayName("updateUser should store a BCrypt-encoded hash when password changes")
    void updateUserShouldStoreBcryptHash() throws Exception {
        catalog.save(User.restore(1, "juan", LEGACY_MD5_ADMIN, true, "Escribano", null, null, null, null, null, 0));
        when(passwordEncoder.encode("newpass")).thenReturn(BCRYPT_HASH);

        mockMvc.perform(put("/api/v1/users/1")
                .contentType("application/json")
                .content("{\"name\":\"juan\",\"password\":\"newpass\",\"type\":\"Escribano\",\"active\":true}"));

        assertThat(catalog.findById(1)).hasValueSatisfying(u -> assertThat(u.passwordHash()).isEqualTo(BCRYPT_HASH));
    }

    @Test
    @DisplayName("login should succeed for a user with a BCrypt-hashed password")
    void loginShouldSucceedForBcryptHash() {
        catalog.save(User.restore(1, "juan", BCRYPT_HASH, true, "Escribano", null, null, null, null, null, 0));
        when(passwordEncoder.matches("secret", BCRYPT_HASH)).thenReturn(true);
        when(jwtTokenService.generateToken("juan")).thenReturn("jwt-token");

        ResponseEntity<?> response = controller.login(
                new UserController.LoginRequest("juan", "secret"), new MockHttpServletResponse());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("valid", true);
        assertThat(catalog.saveCount()).isEqualTo(1); // only the initial seed
    }

    @Test
    @DisplayName("login should reject a legacy MD5 hash (hard cutoff — issue #1762)")
    void loginShouldRejectLegacyMd5Hash() {
        catalog.save(User.restore(1, "admin", LEGACY_MD5_ADMIN, true, "Escribano", null, null, null, null, null, 0));

        ResponseEntity<?> response = controller.login(
                new UserController.LoginRequest("admin", "admin"), new MockHttpServletResponse());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("valid", false);
        assertThat(catalog.findById(1))
                .hasValueSatisfying(u -> assertThat(u.passwordHash()).isEqualTo(LEGACY_MD5_ADMIN));
        assertThat(catalog.saveCount()).isEqualTo(1); // only the initial seed — no upgrade save
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("login should reject a wrong password regardless of stored hash format")
    void loginShouldRejectWrongPassword() {
        catalog.save(User.restore(1, "juan", BCRYPT_HASH, true, "Escribano", null, null, null, null, null, 0));
        when(passwordEncoder.matches(eq("wrong"), eq(BCRYPT_HASH))).thenReturn(false);

        ResponseEntity<?> response = controller.login(
                new UserController.LoginRequest("juan", "wrong"), new MockHttpServletResponse());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("valid", false);
        assertThat(catalog.saveCount()).isEqualTo(1);
        verify(passwordEncoder, never()).encode(any());
    }

    private static final class InMemoryUserCatalog implements UserCatalogPort {
        private final Map<Integer, User> byId = new HashMap<>();
        private final AtomicInteger seq = new AtomicInteger(1);
        private int saves;

        int saveCount() {
            return saves;
        }

        @Override
        public List<User> findAll() {
            return List.copyOf(byId.values());
        }

        @Override
        public Optional<User> findById(int id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<User> findFirstByPersonId(int personId) {
            return byId.values().stream()
                    .filter(u -> Integer.valueOf(personId).equals(u.personId()))
                    .findFirst();
        }

        @Override
        public boolean existsById(int id) {
            return byId.containsKey(id);
        }

        @Override
        public User save(User user) {
            saves++;
            int id = user.id() == null ? seq.getAndIncrement() : user.id();
            User stored = user.withIdentity(id, user.version());
            byId.put(id, stored);
            return stored;
        }

        @Override
        public boolean deleteById(int id) {
            return byId.remove(id) != null;
        }
    }
}
