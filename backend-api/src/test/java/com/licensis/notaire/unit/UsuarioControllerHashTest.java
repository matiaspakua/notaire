package com.licensis.notaire.unit;

import com.licensis.notaire.api.UsuarioController;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.observability.MetricsUtil;
import com.licensis.notaire.repository.UsuarioRepository;
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
@DisplayName("UsuarioController — hashing de contraseñas (issue #554)")
class UsuarioControllerHashTest {

    private static final String LEGACY_MD5_ADMIN = "21232f297a57a5a743894a0e4a801fc3";
    private static final String BCRYPT_HASH = "$2a$12$stub.bcrypt.hash.value.................";

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private MetricsUtil metricsUtil;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new UsuarioController(usuarioRepository, jwtTokenService, metricsUtil, passwordEncoder);
        mockMvc = standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("createUsuario should store a BCrypt-encoded hash, not plaintext or MD5")
    void createUsuarioShouldStoreBcryptHash() throws Exception {
        when(passwordEncoder.encode("secret")).thenReturn(BCRYPT_HASH);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/v1/usuarios").contentType("application/json")
                .content("{\"nombre\":\"juan\",\"contrasenia\":\"secret\",\"tipo\":\"Escribano\",\"activo\":true}"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getContrasenia()).isEqualTo(BCRYPT_HASH);
    }

    @Test
    @DisplayName("updateUsuario should store a BCrypt-encoded hash when password changes")
    void updateUsuarioShouldStoreBcryptHash() throws Exception {
        Usuario existing = new Usuario(1);
        existing.setNombre("juan");
        existing.setContrasenia(LEGACY_MD5_ADMIN);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpass")).thenReturn(BCRYPT_HASH);

        mockMvc.perform(put("/api/v1/usuarios/1").contentType("application/json")
                .content("{\"nombre\":\"juan\",\"contrasenia\":\"newpass\",\"tipo\":\"Escribano\",\"activo\":true}"));

        assertThat(existing.getContrasenia()).isEqualTo(BCRYPT_HASH);
        verify(usuarioRepository).save(existing);
    }

    @Test
    @DisplayName("login should succeed for a user with a BCrypt-hashed password")
    void loginShouldSucceedForBcryptHash() {
        Usuario usuario = new Usuario(1);
        usuario.setNombre("juan");
        usuario.setContrasenia(BCRYPT_HASH);
        usuario.setEstado(true);
        usuario.setTipo("Escribano");
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(passwordEncoder.matches("secret", BCRYPT_HASH)).thenReturn(true);
        when(jwtTokenService.generateToken("juan")).thenReturn("jwt-token");

        DtoUsuario loginRequest = new DtoUsuario();
        loginRequest.setNombre("juan");
        loginRequest.setContrasenia("secret");

        ResponseEntity<?> response = controller.login(loginRequest);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("valido", true);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("login should succeed for a legacy MD5 hash and transparently upgrade it to BCrypt")
    void loginShouldUpgradeLegacyMd5HashOnSuccess() {
        Usuario usuario = new Usuario(1);
        usuario.setNombre("admin");
        usuario.setContrasenia(LEGACY_MD5_ADMIN);
        usuario.setEstado(true);
        usuario.setTipo("Escribano");
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(passwordEncoder.encode("admin")).thenReturn(BCRYPT_HASH);
        when(jwtTokenService.generateToken("admin")).thenReturn("jwt-token");

        DtoUsuario loginRequest = new DtoUsuario();
        loginRequest.setNombre("admin");
        loginRequest.setContrasenia("admin");

        ResponseEntity<?> response = controller.login(loginRequest);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("valido", true);
        assertThat(usuario.getContrasenia()).isEqualTo(BCRYPT_HASH);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("login should reject a wrong password regardless of stored hash format")
    void loginShouldRejectWrongPassword() {
        Usuario usuario = new Usuario(1);
        usuario.setNombre("juan");
        usuario.setContrasenia(BCRYPT_HASH);
        usuario.setEstado(true);
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(passwordEncoder.matches(eq("wrong"), eq(BCRYPT_HASH))).thenReturn(false);

        DtoUsuario loginRequest = new DtoUsuario();
        loginRequest.setNombre("juan");
        loginRequest.setContrasenia("wrong");

        ResponseEntity<?> response = controller.login(loginRequest);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("valido", false);
        verify(usuarioRepository, never()).save(any());
    }
}
