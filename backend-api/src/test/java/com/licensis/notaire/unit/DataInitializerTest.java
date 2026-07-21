package com.licensis.notaire.unit;

import com.licensis.notaire.config.DataInitializer;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import com.licensis.notaire.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializer — usuario admin por defecto")
class DataInitializerTest {

    private static final String BCRYPT_ADMIN = "$2a$12$stub.bcrypt.hash.for.admin.password";

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PersonaRepository personaRepository;
    @Mock
    private TipoIdentificacionRepository tipoIdentificacionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer(
                usuarioRepository, personaRepository, tipoIdentificacionRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should not modify an already-existing admin user (issue #553)")
    void shouldNotModifyExistingAdminUser() {
        Usuario existing = new Usuario();
        existing.setNombre("admin");
        existing.setContrasenia("hash-ya-rotado-por-el-operador");
        existing.setEstado(false);
        when(usuarioRepository.findByNombre("admin")).thenReturn(Optional.of(existing));

        dataInitializer.run(null);

        verify(usuarioRepository, never()).save(any());
        verify(personaRepository, never()).save(any());
        assertThat(existing.getContrasenia()).isEqualTo("hash-ya-rotado-por-el-operador");
        assertThat(existing.getEstado()).isFalse();
    }

    @Test
    @DisplayName("Should create admin user with BCrypt password when none exists")
    void shouldCreateAdminUserWhenNoneExists() {
        when(usuarioRepository.findByNombre("admin")).thenReturn(Optional.empty());
        TipoIdentificacion tipo = new TipoIdentificacion();
        tipo.setNombre("DNI");
        when(tipoIdentificacionRepository.findAll()).thenReturn(List.of(tipo));
        when(passwordEncoder.encode("admin")).thenReturn(BCRYPT_ADMIN);

        dataInitializer.run(null);

        verify(personaRepository).save(any(Persona.class));
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario created = captor.getValue();
        assertThat(created.getNombre()).isEqualTo("admin");
        assertThat(created.getContrasenia()).isEqualTo(BCRYPT_ADMIN);
        assertThat(created.getEstado()).isTrue();
        assertThat(created.getFkIdPersona()).isNotNull();
    }

    @Test
    @DisplayName("Should not propagate exceptions when persistence fails")
    void shouldNotPropagateExceptionsWhenPersistenceFails() {
        when(usuarioRepository.findByNombre("admin")).thenThrow(new RuntimeException("DB down"));

        // Must not throw — startup should never be blocked by the seeder.
        dataInitializer.run(null);

        verify(usuarioRepository, never()).save(any());
    }
}
