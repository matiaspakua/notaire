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

    /** MD5("admin") — debe coincidir con la verificación de UsuarioController#login. */
    private static final String MD5_ADMIN = "21232f297a57a5a743894a0e4a801fc3";

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PersonaRepository personaRepository;
    @Mock
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer(usuarioRepository, personaRepository, tipoIdentificacionRepository);
    }

    @Test
    @DisplayName("Should reset password and activate existing admin user")
    void shouldResetPasswordAndActivateExistingAdminUser() {
        Usuario existing = new Usuario();
        existing.setNombre("admin");
        existing.setContrasenia("hash-obsoleto");
        existing.setEstado(false);
        when(usuarioRepository.findByNombre("admin")).thenReturn(Optional.of(existing));

        dataInitializer.run(null);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario saved = captor.getValue();
        assertThat(saved.getContrasenia()).isEqualTo(MD5_ADMIN);
        assertThat(saved.getEstado()).isTrue();
        verify(personaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create admin user with MD5 password when none exists")
    void shouldCreateAdminUserWhenNoneExists() {
        when(usuarioRepository.findByNombre("admin")).thenReturn(Optional.empty());
        TipoIdentificacion tipo = new TipoIdentificacion();
        tipo.setNombre("DNI");
        when(tipoIdentificacionRepository.findAll()).thenReturn(List.of(tipo));

        dataInitializer.run(null);

        verify(personaRepository).save(any(Persona.class));
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario created = captor.getValue();
        assertThat(created.getNombre()).isEqualTo("admin");
        assertThat(created.getContrasenia()).isEqualTo(MD5_ADMIN);
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
