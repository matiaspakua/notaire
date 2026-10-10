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
    void presupuestosPaymentsDomain() throws Exception {
        mockMvc.perform(get("/api/v1/budgets")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/items")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/payments")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/budgets/person/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/items/budget/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/payments/budget/1")).andExpect(status().isOk());
    }

    @Test
    void gestionesDomain() throws Exception {
        mockMvc.perform(get("/api/v1/managements")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/managements/client/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/history")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/history/management/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/management-statuses")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/procedures")).andExpect(status().isOk());
    }

    @Test
    void clientesPersonsDomain() throws Exception {
        mockMvc.perform(get("/api/v1/people")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/people/search")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/people/search").param("firstName", "Admin")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/people/search").param("idIdentificationType", "1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/identification-types")).andExpect(status().isOk());
    }

    @Test
    void documentacionDomain() throws Exception {
        mockMvc.perform(get("/api/v1/submitted-documents")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/document-types")).andExpect(status().isOk());
    }

    @Test
    void escriturasDomain() throws Exception {
        mockMvc.perform(get("/api/v1/deeds")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/properties")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/copies")).andExpect(status().isOk());
    }

    @Test
    void testimoniosDomain() throws Exception {
        mockMvc.perform(get("/api/v1/testimonies")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/testimony-movements")).andExpect(status().isOk());
    }

    @Test
    void administracionCatalogosDomain() throws Exception {
        mockMvc.perform(get("/api/v1/procedure-types")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/folio-types")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/concepts")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/budget-templates")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/folios")).andExpect(status().isOk());
    }

    @Test
    void usersAuditDomain() throws Exception {
        mockMvc.perform(get("/api/v1/users")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/audit-log")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/audit-log/user/1")).andExpect(status().isOk());
    }

    @Test
    void suplenciasDomain() throws Exception {
        mockMvc.perform(get("/api/v1/substitutions")).andExpect(status().isOk());
    }
}
