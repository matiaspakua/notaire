package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.RegistroAuditoriaRepository;
import com.licensis.notaire.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("AuditoriaFlow end-to-end integration test")
class AuditoriaFlowIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RegistroAuditoriaRepository auditRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Optional<Usuario> admin = usuarioRepository.findAll().stream()
                .filter(u -> "admin".equalsIgnoreCase(u.getNombre()))
                .findFirst();
        admin.ifPresent(usuario -> SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        usuario.getNombre(), "n/a",
                        AuthorityUtils.createAuthorityList("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should write an audit row when authenticated user performs a business mutation")
    void shouldWriteAuditRowWhenAuthenticatedHitController() throws Exception {
        long before = auditRepository.count();

        // Only state-changing operations are audited; a POST (create) qualifies.
        mockMvc.perform(post("/api/v1/estado-gestion")
                        .contentType("application/json")
                        .content("{\"nombre\":\"Auditoría IT\",\"observaciones\":\"flujo de auditoría\"}"))
                .andExpect(status().isCreated());

        long after = auditRepository.count();
        assertThat(after).isGreaterThan(before);

        // The latest audit row must describe an "EstadosDeGestion" creation.
        boolean hasMatch = auditRepository.findAll().stream()
                .anyMatch(r -> "EstadosDeGestion".equals(r.getModulo())
                        && r.getDetalleOperacion() != null
                        && r.getDetalleOperacion().toLowerCase().contains("creación"));
        assertThat(hasMatch).isTrue();
    }

    @Test
    @DisplayName("Should NOT audit read-only GET operations")
    void shouldNotAuditReadOnlyGets() throws Exception {
        long before = auditRepository.count();

        // GET is a read; it must not produce an audit record.
        mockMvc.perform(get("/api/v1/conceptos"))
                .andExpect(status().isOk());

        long after = auditRepository.count();
        assertThat(after).isEqualTo(before);
    }

    @Test
    @DisplayName("Should NOT audit reads of the audit endpoint itself")
    void shouldNotAuditReadsOfTheAuditEndpoint() throws Exception {
        long before = auditRepository.count();

        mockMvc.perform(get("/api/v1/registro-auditoria"))
                .andExpect(status().isOk());

        long after = auditRepository.count();
        assertThat(after).isEqualTo(before);
    }

    @Test
    @DisplayName("Should expose audit records via paginated DTO endpoint")
    void shouldExposeAuditRecordsViaPaginatedDtoEndpoint() throws Exception {
        // First, generate an audit row.
        mockMvc.perform(get("/api/v1/conceptos")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/registro-auditoria?page=0&size=5&sort=fecha,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    @DisplayName("Should support filtering audit records by modulo")
    void shouldFilterAuditRecordsByModulo() throws Exception {
        mockMvc.perform(get("/api/v1/conceptos")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/registro-auditoria?page=0&size=10&modulo=Conceptos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
