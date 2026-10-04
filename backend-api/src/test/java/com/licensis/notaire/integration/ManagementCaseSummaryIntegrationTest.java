package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Issue #774 — GET /api/v1/gestiones/{id}/resumen-caso over the H2 schema. */
@RequirementCoverage({"CU07", "CU11", "CU12", "CU70"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Resumen del caso de una gestión — integration tests")
class ManagementCaseSummaryIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DeedManagementRepository managementRepository;

    @Autowired
    private ManagementStatusRepository statusRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer managementWithoutDeeds() {
        ManagementStatus status = new ManagementStatus();
        status.setName("Estado resumen caso " + System.nanoTime());
        status = statusRepository.save(status);

        DeedManagement management = new DeedManagement();
        management.setDateStart(new Date());
        management.setEncabezado("Gestión sin escritura");
        management.setNumber((int) (System.nanoTime() % 1_000_000));
        management.setFkIdManagementStatus(status);
        return managementRepository.save(management).getIdManagement();
    }

    @Test
    @DisplayName("Should return the header and an empty deeds list when no trámite has a deed")
    void shouldReturnEmptyDeedsList() throws Exception {
        Integer id = managementWithoutDeeds();

        mockMvc.perform(get("/api/v1/gestiones/" + id + "/resumen-caso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managementId").value(id))
                .andExpect(jsonPath("$.deeds").isEmpty());
    }

    @Test
    @DisplayName("Should return 404 for an unknown gestión")
    void shouldReturn404ForUnknownManagement() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/999999/resumen-caso")).andExpect(status().isNotFound());
    }
}
