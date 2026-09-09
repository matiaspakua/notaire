package com.licensis.notaire.unit;

import com.licensis.notaire.api.UserController;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.business.User;
import com.licensis.notaire.observability.MetricsUtil;
import com.licensis.notaire.repository.UserRepository;
import com.licensis.notaire.security.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController — hashing de contraseñas (issue #554)")
class UserControllerHashTest {

    private static final String LEGACY_MD5_ADMIN = "21232f297a57a5a743894a0e4a801fc3";
    private static final String BCRYPT_HASH = "$2a$12$stub.bcrypt.hash.value.................";

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private MetricsUtil metricsUtil;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new UserController(userRepository, jwtTokenService, metricsUtil, passwordEncoder,
                new LoginAttemptService(5, 900000));
        mockMvc = standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("createUsuario should store a BCrypt-encoded hash, not plaintext or MD5")
    void createUserShouldStoreBcryptHash() throws Exception {
        when(passwordEncoder.encode("secret")).thenReturn(BCRYPT_HASH);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/v1/usuarios").contentType("application/json")
                .content("{\"name\":\"juan\",\"password\":\"secret\",\"type\":\"Escribano\",\"active\":true}"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo(BCRYPT_HASH);
    }

    @Test
    @DisplayName("updateUsuario should store a BCrypt-encoded hash when password changes")
    void updateUserShouldStoreBcryptHash() throws Exception {
        User existing = new User(1);
        existing.setName("juan");
        existing.setPassword(LEGACY_MD5_ADMIN);
        when(userRepository.findById(1)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpass")).thenReturn(BCRYPT_HASH);

        mockMvc.perform(put("/api/v1/usuarios/1").contentType("application/json")
                .content("{\"name\":\"juan\",\"password\":\"newpass\",\"type\":\"Escribano\",\"active\":true}"));

        assertThat(existing.getPassword()).isEqualTo(BCRYPT_HASH);
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("login should succeed for a user with a BCrypt-hashed password")
    void loginShouldSucceedForBcryptHash() {
        User user = new User(1);
        user.setName("juan");
        user.setPassword(BCRYPT_HASH);
        user.setStatus(true);
        user.setType("Escribano");
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(passwordEncoder.matches("secret", BCRYPT_HASH)).thenReturn(true);
        when(jwtTokenService.generateToken("juan")).thenReturn("jwt-token");

        DtoUser loginRequest = new DtoUser();
        loginRequest.setName("juan");
        loginRequest.setPassword("secret");

        ResponseEntity<?> response = controller.login(loginRequest);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("valido", true);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login should succeed for a legacy MD5 hash and transparently upgrade it to BCrypt")
    void loginShouldUpgradeLegacyMd5HashOnSuccess() {
        User user = new User(1);
        user.setName("admin");
        user.setPassword(LEGACY_MD5_ADMIN);
        user.setStatus(true);
        user.setType("Escribano");
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(passwordEncoder.encode("admin")).thenReturn(BCRYPT_HASH);
        when(jwtTokenService.generateToken("admin")).thenReturn("jwt-token");

        DtoUser loginRequest = new DtoUser();
        loginRequest.setName("admin");
        loginRequest.setPassword("admin");

        ResponseEntity<?> response = controller.login(loginRequest);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("valido", true);
        assertThat(user.getPassword()).isEqualTo(BCRYPT_HASH);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("login should reject a wrong password regardless of stored hash format")
    void loginShouldRejectWrongPassword() {
        User user = new User(1);
        user.setName("juan");
        user.setPassword(BCRYPT_HASH);
        user.setStatus(true);
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(passwordEncoder.matches(eq("wrong"), eq(BCRYPT_HASH))).thenReturn(false);

        DtoUser loginRequest = new DtoUser();
        loginRequest.setName("juan");
        loginRequest.setPassword("wrong");

        ResponseEntity<?> response = controller.login(loginRequest);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("valido", false);
        verify(userRepository, never()).save(any());
    }
}
