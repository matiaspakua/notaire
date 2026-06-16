package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Rol;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.RolRepository;
import com.licensis.notaire.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Usuario Repository Integration Tests")
class UsuarioRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Rol rolAdmin;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();

        rolAdmin = new Rol();
        rolAdmin.setNombre("ADMIN");
        rolAdmin.setActivo(true);
        rolAdmin = rolRepository.save(rolAdmin);
    }

    @Test
    @DisplayName("Should create user with role")
    void shouldCreateUserWithRole() {
        Usuario usuario = new Usuario();
        usuario.setNombre("testuser");
        usuario.setContrasenia("securepass");
        usuario.setEstado(true);
        usuario.setTipo("ADMIN");
        usuario.setRol(rolAdmin);

        Usuario saved = usuarioRepository.save(usuario);

        assertThat(saved.getIdUsuario()).isNotNull();
        assertThat(saved.getNombre()).isEqualTo("testuser");
        assertThat(saved.getRol().getIdRol()).isEqualTo(rolAdmin.getIdRol());
    }

    @Test
    @DisplayName("Should retrieve user by ID")
    void shouldRetrieveUserById() {
        Usuario usuario = new Usuario();
        usuario.setNombre("user123");
        usuario.setContrasenia("pass123");
        usuario.setEstado(true);
        usuario.setTipo("USER");
        usuario.setRol(rolAdmin);
        Usuario saved = usuarioRepository.save(usuario);

        Optional<Usuario> found = usuarioRepository.findById(saved.getIdUsuario());

        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("user123");
    }

    @Test
    @DisplayName("Should update user status")
    void shouldUpdateUserStatus() {
        Usuario usuario = new Usuario();
        usuario.setNombre("statususer");
        usuario.setContrasenia("pass");
        usuario.setEstado(true);
        usuario.setTipo("USER");
        usuario.setRol(rolAdmin);
        Usuario saved = usuarioRepository.save(usuario);

        saved.setEstado(false);
        Usuario updated = usuarioRepository.save(saved);

        assertThat(updated.getEstado()).isFalse();
    }

    @Test
    @DisplayName("Should support multiple users")
    void shouldSupportMultipleUsers() {
        for (int i = 0; i < 5; i++) {
            Usuario usuario = new Usuario();
            usuario.setNombre("user" + i);
            usuario.setContrasenia("pass" + i);
            usuario.setEstado(true);
            usuario.setTipo("USER");
            usuario.setRol(rolAdmin);
            usuarioRepository.save(usuario);
        }

        List<Usuario> all = usuarioRepository.findAll();
        assertThat(all).hasSize(5);
    }
}
