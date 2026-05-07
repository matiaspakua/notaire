package com.licensis.notaire.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for all domains in the traceability matrix.
 * Ensures each use-case domain has at least one working endpoint (GET list or key route).
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@Sql(scripts = "classpath:cleanup-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class UseCaseDomainsIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void presupuestosPagosDomain() throws Exception {
        mockMvc.perform(get("/api/v1/presupuestos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/items")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/pagos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/presupuestos/persona/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/items/presupuesto/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/pagos/presupuesto/1")).andExpect(status().isOk());
    }

    @Test
    void gestionesDomain() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/gestiones/cliente/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/historial")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/historial/gestion/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/estado-gestion")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/tramites")).andExpect(status().isOk());
    }

    @Test
    void clientesPersonasDomain() throws Exception {
        mockMvc.perform(get("/api/v1/personas")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/personas/buscar")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/personas/buscar").param("nombre", "Admin")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/personas/buscar").param("idTipoIdentificacion", "1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/tipo-identificacion")).andExpect(status().isOk());
    }

    @Test
    void documentacionDomain() throws Exception {
        mockMvc.perform(get("/api/v1/documento-presentado")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/tipo-de-documento")).andExpect(status().isOk());
    }

    @Test
    void escriturasDomain() throws Exception {
        mockMvc.perform(get("/api/v1/escrituras")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/inmueble")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/copia")).andExpect(status().isOk());
    }

    @Test
    void testimoniosDomain() throws Exception {
        mockMvc.perform(get("/api/v1/testimonio")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/movimiento-testimonio")).andExpect(status().isOk());
    }

    @Test
    void administracionCatalogosDomain() throws Exception {
        mockMvc.perform(get("/api/v1/tipo-tramite")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/tipo-folio")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/conceptos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/plantilla-presupuestos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/folio")).andExpect(status().isOk());
    }

    @Test
    void usuariosAuditoriaDomain() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/registro-auditoria")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/registro-auditoria/usuario/1")).andExpect(status().isOk());
    }

    @Test
    void suplenciasDomain() throws Exception {
        mockMvc.perform(get("/api/v1/suplencia")).andExpect(status().isOk());
    }
}
