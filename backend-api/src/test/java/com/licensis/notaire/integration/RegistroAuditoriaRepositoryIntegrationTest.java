package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.RegistroAuditoriaRepository;
import com.licensis.notaire.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RegistroAuditoriaRepository Integration Tests")
class RegistroAuditoriaRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    private RegistroAuditoriaRepository registroAuditoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private RegistroAuditoria testRegistro;
    private Usuario testUsuario;

    @BeforeEach
    void setUp() {
        testUsuario = new Usuario();
        testUsuario.setNombre("Admin User");
        testUsuario.setTipo("ADMIN");
        testUsuario.setEstado(true);
        testUsuario.setContrasenia("password123");
        usuarioRepository.save(testUsuario);

        testRegistro = new RegistroAuditoria();
        testRegistro.setModulo("DOCUMENTOS");
        testRegistro.setDetalleOperacion("CREATE");
        testRegistro.setFecha(new Date());
        testRegistro.setFkIdUsuario(testUsuario);
    }

    @Test
    @DisplayName("Should persist and retrieve registro auditoria")
    void shouldPersistAndRetrieveRegistro() {
        RegistroAuditoria saved = registroAuditoriaRepository.save(testRegistro);

        assertThat(saved.getIdRegistroAuditoria()).isNotNull();

        Optional<RegistroAuditoria> retrieved = registroAuditoriaRepository.findById(saved.getIdRegistroAuditoria());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r.getModulo()).isEqualTo("DOCUMENTOS");
                    assertThat(r.getDetalleOperacion()).isEqualTo("CREATE");
                });
    }

    @Test
    @DisplayName("Should find registros by modulo")
    void shouldFindByModulo() {
        registroAuditoriaRepository.save(testRegistro);

        Pageable pageable = PageRequest.of(0, 10);
        Page<RegistroAuditoria> found = registroAuditoriaRepository.findByModulo("DOCUMENTOS", pageable);

        assertThat(found).isNotEmpty()
                .allMatch(r -> r.getModulo().equals("DOCUMENTOS"));
    }

    @Test
    @DisplayName("Should find registros by usuario id")
    void shouldFindByUsuarioId() {
        registroAuditoriaRepository.save(testRegistro);

        List<RegistroAuditoria> found = registroAuditoriaRepository.findByFkIdUsuarioIdUsuario(testUsuario.getIdUsuario());

        assertThat(found).isNotEmpty()
                .allMatch(r -> r.getFkIdUsuario().getIdUsuario().equals(testUsuario.getIdUsuario()));
    }

    @Test
    @DisplayName("Should find registros by usuario id paginated")
    void shouldFindByUsuarioIdPaginated() {
        registroAuditoriaRepository.save(testRegistro);

        RegistroAuditoria registro2 = new RegistroAuditoria();
        registro2.setModulo("USUARIOS");
        registro2.setDetalleOperacion("UPDATE");
        registro2.setFecha(new Date());
        registro2.setFkIdUsuario(testUsuario);
        registroAuditoriaRepository.save(registro2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<RegistroAuditoria> found = registroAuditoriaRepository.findByFkIdUsuarioIdUsuario(testUsuario.getIdUsuario(), pageable);

        assertThat(found).hasSize(2)
                .allMatch(r -> r.getFkIdUsuario().getIdUsuario().equals(testUsuario.getIdUsuario()));
    }

    @Test
    @DisplayName("Should find all registros with usuario")
    void shouldFindAllWithUsuario() {
        registroAuditoriaRepository.save(testRegistro);

        List<RegistroAuditoria> all = registroAuditoriaRepository.findAllWithUsuario();

        assertThat(all).isNotEmpty()
                .anyMatch(r -> r.getIdRegistroAuditoria().equals(testRegistro.getIdRegistroAuditoria()));
    }

    @Test
    @DisplayName("Should find registro by id with usuario")
    void shouldFindByIdWithUsuario() {
        RegistroAuditoria saved = registroAuditoriaRepository.save(testRegistro);

        Optional<RegistroAuditoria> retrieved = registroAuditoriaRepository.findByIdWithUsuario(saved.getIdRegistroAuditoria());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r.getFkIdUsuario()).isNotNull();
                    assertThat(r.getFkIdUsuario().getIdUsuario()).isEqualTo(testUsuario.getIdUsuario());
                });
    }

    @Test
    @DisplayName("Should find registros by usuario id with usuario")
    void shouldFindByUsuarioIdWithUsuario() {
        registroAuditoriaRepository.save(testRegistro);

        List<RegistroAuditoria> found = registroAuditoriaRepository.findByUsuarioIdWithUsuario(testUsuario.getIdUsuario());

        assertThat(found).isNotEmpty()
                .allMatch(r -> r.getFkIdUsuario().getIdUsuario().equals(testUsuario.getIdUsuario()));
    }

    @Test
    @DisplayName("Should maintain referential integrity with usuario")
    void shouldMaintainReferentialIntegrityWithUsuario() {
        RegistroAuditoria saved = registroAuditoriaRepository.save(testRegistro);

        Optional<RegistroAuditoria> retrieved = registroAuditoriaRepository.findByIdWithUsuario(saved.getIdRegistroAuditoria());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r.getFkIdUsuario()).isNotNull();
                    assertThat(r.getFkIdUsuario().getNombre()).isEqualTo("Admin User");
                });
    }

    @Test
    @DisplayName("Should update registro auditoria")
    void shouldUpdateRegistro() {
        RegistroAuditoria saved = registroAuditoriaRepository.save(testRegistro);

        saved.setDetalleOperacion("DELETE");
        registroAuditoriaRepository.save(saved);

        Optional<RegistroAuditoria> updated = registroAuditoriaRepository.findById(saved.getIdRegistroAuditoria());

        assertThat(updated).isPresent()
                .hasValueSatisfying(r -> assertThat(r.getDetalleOperacion()).isEqualTo("DELETE"));
    }

    @Test
    @DisplayName("Should delete registro auditoria")
    void shouldDeleteRegistro() {
        RegistroAuditoria saved = registroAuditoriaRepository.save(testRegistro);

        registroAuditoriaRepository.deleteById(saved.getIdRegistroAuditoria());

        Optional<RegistroAuditoria> deleted = registroAuditoriaRepository.findById(saved.getIdRegistroAuditoria());

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should find all registros")
    void shouldFindAll() {
        registroAuditoriaRepository.save(testRegistro);

        List<RegistroAuditoria> all = registroAuditoriaRepository.findAll();

        assertThat(all).isNotEmpty()
                .anyMatch(r -> r.getIdRegistroAuditoria().equals(testRegistro.getIdRegistroAuditoria()));
    }
}
