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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.ConceptRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;

/**
 * Issue #574: pins the budget-templates API (statuses and success bodies) so moving it from the
 * legacy JPA controller to a service keeps the behaviour. Written against the old implementation.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Budget templates API behaviour (issue #574)")
class BudgetTemplateApiCharacterizationTest {

    private static final String BASE = "/api/v1/budget-templates";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    private MockMvc mockMvc;
    private Integer procedureTypeId;
    private Integer conceptId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        ProcedureType procedureType = new ProcedureType();
        procedureType.setName("Template API " + System.nanoTime());
        procedureTypeId = procedureTypeRepository.save(procedureType).getIdProcedureType();
        Concept concept = new Concept();
        concept.setName("Concept API " + System.nanoTime());
        concept.setValue(new java.math.BigDecimal("10"));
        concept.setPercentage(0);
        conceptId = conceptRepository.save(concept).getIdConcept();
    }

    private String body(Integer type, Integer concept, String notes) {
        return new StringBuilder("{")
                .append(type == null ? "" : "\"fkIdProcedureType\": " + type + ",")
                .append(concept == null ? "" : "\"fkIdConcept\": " + concept + ",")
                .append("\"notes\": \"").append(notes).append("\"}")
                .toString();
    }

    private void createTemplate(String notes) throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(body(procedureTypeId, conceptId, notes)))
                .andExpect(status().isCreated());
    }

    private String memberUrl() {
        return BASE + "/procedure-types/" + procedureTypeId + "/concept/" + conceptId;
    }

    @Test
    @DisplayName("create answers 201 with both ids and the template is listed")
    void createsAndLists() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(body(procedureTypeId, conceptId, "first")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fkIdProcedureType").value(procedureTypeId))
                .andExpect(jsonPath("$.fkIdConcept").value(conceptId));

        mockMvc.perform(get(BASE + "/procedure-types/" + procedureTypeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].notes").value("first"))
                .andExpect(jsonPath("$[0].fkIdConcept").value(conceptId));
        mockMvc.perform(get(BASE)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("create accepts the nested budgetTemplatePK form")
    void createsWithNestedKey() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content("{\"budgetTemplatePK\": {\"fkIdProcedureType\": " + procedureTypeId
                        + ", \"fkIdConcept\": " + conceptId + "}, \"notes\": \"nested\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("create without ids answers 400")
    void rejectsMissingIds() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(body(null, conceptId, "x")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create with an unknown concept or procedure type answers 400")
    void rejectsUnknownReferences() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(body(procedureTypeId, 987654, "x")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(body(987654, conceptId, "x")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("creating the same template twice answers 409")
    void rejectsDuplicates() throws Exception {
        createTemplate("once");

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(body(procedureTypeId, conceptId, "twice")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("update changes the notes and answers 200")
    void updatesNotes() throws Exception {
        createTemplate("before");

        mockMvc.perform(put(memberUrl()).contentType(MediaType.APPLICATION_JSON)
                .content(body(procedureTypeId, conceptId, "after")))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE + "/procedure-types/" + procedureTypeId))
                .andExpect(jsonPath("$[0].notes").value("after"));
    }

    @Test
    @DisplayName("update of an unknown template answers 404")
    void updateUnknownIsNotFound() throws Exception {
        mockMvc.perform(put(memberUrl()).contentType(MediaType.APPLICATION_JSON)
                .content(body(procedureTypeId, conceptId, "x")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("delete answers 204 and the template disappears; deleting again answers 404")
    void deletes() throws Exception {
        createTemplate("gone");

        mockMvc.perform(delete(memberUrl())).andExpect(status().isNoContent());
        mockMvc.perform(get(BASE + "/procedure-types/" + procedureTypeId))
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(delete(memberUrl())).andExpect(status().isNotFound());
    }
}
