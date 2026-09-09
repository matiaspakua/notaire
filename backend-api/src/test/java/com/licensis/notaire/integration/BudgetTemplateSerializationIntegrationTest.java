package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@code @DirtiesContext(BEFORE_CLASS)}: the create/list round trip goes through the legacy
 * {@code PlantillaPresupuestoJpaController}, which caches its {@code EntityManagerFactory} in
 * the static {@link com.licensis.notaire.config.JpaControllerProvider}. If an earlier test class
 * in the same JVM dirtied the Spring context, that static reference goes stale relative to the
 * newly created H2 instance, so a create() here is invisible to a getAll() backed by the fresh
 * context. Forcing a clean context before this class keeps the static EMF and the active context
 * in sync.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("PlantillaPresupuesto — CU30 list endpoints serialize without cyclic recursion")
class BudgetTemplateSerializationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createConcept() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/conceptos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Concept Ciclo IT", "value": 100, "percentage": 0, "fixedConcept": true}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idConcept").asInt();
    }

    private Integer createTypeProcedure() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/tipo-tramite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Procedure Ciclo IT", "isRegistered": false, "isArchived": false, "associatesProperties": false}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idProcedureType").asInt();
    }

    private void createTemplate(Integer typeProcedureId, Integer conceptId) throws Exception {
        String body = """
                {"budgetTemplatePK": {"fkIdProcedureType": %d, "fkIdConcept": %d},
                 "procedureType": {"idProcedureType": %d}, "concept": {"idConcept": %d},
                 "notes": "template ciclo IT"}
                """.formatted(typeProcedureId, conceptId, typeProcedureId, conceptId);
        mockMvc.perform(post("/api/v1/plantilla-presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should serialize plantilla-presupuestos list as valid JSON array without recursion")
    void shouldListPlantillasWithoutCyclicRecursion() throws Exception {
        Integer conceptId = createConcept();
        Integer typeProcedureId = createTypeProcedure();
        createTemplate(typeProcedureId, conceptId);

        MvcResult result = mockMvc.perform(get("/api/v1/plantilla-presupuestos"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.isArray()).isTrue();
        JsonNode template = null;
        for (JsonNode candidate : body) {
            JsonNode pk = candidate.get("plantillaPresupuestoPK");
            if (pk != null && pk.get("fkIdConcepto").asInt() == conceptId
                    && pk.get("fkIdTipoTramite").asInt() == typeProcedureId) {
                template = candidate;
                break;
            }
        }
        assertThat(template).as("created plantilla should be in the list").isNotNull();
        JsonNode concept = template.get("concepto");
        assertThat(concept).isNotNull();
        assertThat(concept.has("plantillaPresupuestoList"))
                .as("concepto must not embed its plantillaPresupuestoList (cyclic reference)")
                .isFalse();
    }

    @Test
    @DisplayName("Should serialize conceptos list as valid JSON when a plantilla references the concepto")
    void shouldListConceptosWhenTemplateExists() throws Exception {
        Integer conceptId = createConcept();
        Integer typeProcedureId = createTypeProcedure();
        createTemplate(typeProcedureId, conceptId);

        MvcResult result = mockMvc.perform(get("/api/v1/conceptos"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.isArray()).isTrue();
    }
}
