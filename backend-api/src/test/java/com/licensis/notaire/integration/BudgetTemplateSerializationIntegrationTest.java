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
 * {@code BudgetTemplateJpaController}, which caches its {@code EntityManagerFactory} in
 * the static {@link com.licensis.notaire.config.JpaControllerProvider}. If an earlier test class
 * in the same JVM dirtied the Spring context, that static reference goes stale relative to the
 * newly created H2 instance, so a create() here is invisible to a getAll() backed by the fresh
 * context. Forcing a clean context before this class keeps the static EMF and the active context
 * in sync.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("BudgetTemplate — CU30 list endpoints serialize without cyclic recursion")
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
        MvcResult result = mockMvc.perform(post("/api/v1/concepts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Concept Cycle IT", "value": 100, "percentage": 0, "fixedConcept": true}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idConcept").asInt();
    }

    private Integer createTypeProcedure() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/procedure-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Procedure Cycle IT", "isRegistered": false, "isArchived": false, "associatesProperties": false}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idProcedureType").asInt();
    }

    private void createTemplate(Integer typeProcedureId, Integer conceptId) throws Exception {
        String body = """
                {"fkIdProcedureType": %d, "fkIdConcept": %d, "notes": "template cycle IT"}
                """.formatted(typeProcedureId, conceptId);
        mockMvc.perform(post("/api/v1/budget-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should serialize the budget-template list as valid JSON without recursion")
    void shouldListTemplatesWithoutCyclicRecursion() throws Exception {
        Integer conceptId = createConcept();
        Integer typeProcedureId = createTypeProcedure();
        createTemplate(typeProcedureId, conceptId);

        MvcResult result = mockMvc.perform(get("/api/v1/budget-templates"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.isArray()).isTrue();
        JsonNode template = null;
        for (JsonNode candidate : body) {
            if (candidate.path("fkIdConcept").asInt() == conceptId
                    && candidate.path("fkIdProcedureType").asInt() == typeProcedureId) {
                template = candidate;
                break;
            }
        }
        assertThat(template).as("created template should be in the list").isNotNull();
        assertThat(template.has("concept"))
                .as("DTO response must not embed concept entity graph")
                .isFalse();
        assertThat(template.has("budgetTemplateList"))
                .as("DTO response must not embed cyclic budgetTemplateList")
                .isFalse();
    }

    @Test
    @DisplayName("Should serialize concepts list as valid JSON when a template references the concept")
    void shouldListConceptsWhenTemplateExists() throws Exception {
        Integer conceptId = createConcept();
        Integer typeProcedureId = createTypeProcedure();
        createTemplate(typeProcedureId, conceptId);

        MvcResult result = mockMvc.perform(get("/api/v1/concepts"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.isArray()).isTrue();
    }
}
