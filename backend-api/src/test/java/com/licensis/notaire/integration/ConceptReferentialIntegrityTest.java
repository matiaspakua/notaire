package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("CU34/CU37/CU66 — Concepto referential integrity (issue #432)")
class ConceptReferentialIntegrityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createConcept(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/conceptos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Test",
                                  "value": 100,
                                  "enabled": true,
                                  "percentage": 0,
                                  "fixedConcept": false
                                }
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idConcept").asInt();
    }

    private void linkConceptToTemplate(int typeProcedureId, int conceptId) throws Exception {
        mockMvc.perform(post("/api/v1/plantilla-presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "budgetTemplatePK": {
                                    "fkIdProcedureType": %d,
                                    "fkIdConcept": %d
                                  },
                                  "procedureType": {"idProcedureType": %d},
                                  "concept": {"idConcept": %d}
                                }
                                """.formatted(typeProcedureId, conceptId, typeProcedureId, conceptId)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return inUse=false when no plantilla references the concepto")
    void shouldReturnInUseFalseWhenConceptIsNotReferenced() throws Exception {
        int id = createConcept("ConceptoFree_InUse");

        mockMvc.perform(get("/api/v1/conceptos/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("Should return inUse=true when a plantilla references the concepto")
    void shouldReturnInUseTrueWhenConceptIsReferenced() throws Exception {
        int id = createConcept("ConceptoUsed_InUse");
        linkConceptToTemplate(1, id);

        mockMvc.perform(get("/api/v1/conceptos/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(true));
    }

    @Test
    @DisplayName("Should return 409 when editing a concepto that is in use")
    void shouldReturn409WhenEditingConceptInUse() throws Exception {
        int id = createConcept("ConceptoEditBlocked");
        linkConceptToTemplate(1, id);

        mockMvc.perform(put("/api/v1/conceptos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "NameModificado",
                                  "description": "Updated",
                                  "value": 200,
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should allow editing a concepto that is NOT in use")
    void shouldAllowEditingConceptNotInUse() throws Exception {
        int id = createConcept("ConceptoEditAllowed");

        mockMvc.perform(put("/api/v1/conceptos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "ConceptEditAllowedUpdated",
                                  "description": "Updated description",
                                  "value": 200,
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 409 when deleting a concepto that is in use")
    void shouldReturn409WhenDeletingConceptInUse() throws Exception {
        int id = createConcept("ConceptoDeleteBlocked");
        linkConceptToTemplate(1, id);

        mockMvc.perform(delete("/api/v1/conceptos/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should search conceptos by name")
    void shouldSearchConceptosByName() throws Exception {
        createConcept("BuscarEsteConcepto");

        mockMvc.perform(get("/api/v1/conceptos/search").param("name", "BuscarEste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("BuscarEsteConcepto"));
    }
}
