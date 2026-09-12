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
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.BudgetTemplatePK;
import com.licensis.notaire.repository.BudgetTemplateRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("TipoDeTramite — referential integrity checks")
class ProcedureTypeReferentialIntegrityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BudgetTemplateRepository budgetTemplateRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return inUse=false when type de tramite is not referenced")
    void shouldReturnInUseFalseWhenNotReferenced() throws Exception {
        int id = createTypeProcedure("Tramite_unused_" + System.nanoTime());

        mockMvc.perform(get("/api/v1/tipo-tramite/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("Should return inUse=true when type de tramite is referenced by plantilla budget")
    void shouldReturnInUseTrueWhenReferencedByBudgetTemplate() throws Exception {
        int id = createTypeProcedure("Tramite_used_" + System.nanoTime());
        linkToBudgetTemplate(id);

        mockMvc.perform(get("/api/v1/tipo-tramite/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(true));
    }

    @Test
    @DisplayName("Should return 409 when editing type de tramite that is in use")
    void shouldReturn409WhenEditingTypeProcedureInUse() throws Exception {
        int id = createTypeProcedure("Tramite_edit_conflict_" + System.nanoTime());
        linkToBudgetTemplate(id);

        String updateBody = """
                {"name": "Name actualizado", "isArchived": false, "isRegistered": false, "associatesProperties": false}
                """;

        mockMvc.perform(put("/api/v1/tipo-tramite/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should allow editing type de tramite that is not in use")
    void shouldAllowEditingTypeProcedureNotInUse() throws Exception {
        int id = createTypeProcedure("Tramite_edit_ok_" + System.nanoTime());

        String updateBody = """
                {"name": "Name actualizado ok", "isArchived": false, "isRegistered": false, "associatesProperties": false}
                """;

        mockMvc.perform(put("/api/v1/tipo-tramite/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 409 when deleting type de tramite that is in use")
    void shouldReturn409WhenDeletingTypeProcedureInUse() throws Exception {
        int id = createTypeProcedure("Tramite_delete_conflict_" + System.nanoTime());
        linkToBudgetTemplate(id);

        mockMvc.perform(delete("/api/v1/tipo-tramite/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should search tipos de tramite by name")
    void shouldSearchTiposProcedureByName() throws Exception {
        String uniqueName = "SearchableTramite_" + System.nanoTime();
        createTypeProcedure(uniqueName);

        mockMvc.perform(get("/api/v1/tipo-tramite/search")
                        .param("name", "SearchableTramite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name =~ /.*SearchableTramite.*/)]").exists());
    }

    private int createTypeProcedure(String name) throws Exception {
        String body = String.format("""
                {"name": "%s", "isArchived": false, "isRegistered": false, "associatesProperties": false}
                """, name);

        MvcResult result = mockMvc.perform(post("/api/v1/tipo-tramite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return mapper.readTree(result.getResponse().getContentAsString())
                .get("idProcedureType").asInt();
    }

    private void linkToBudgetTemplate(int idProcedureType) {
        BudgetTemplatePK pk = new BudgetTemplatePK(idProcedureType, 1);
        BudgetTemplate template = new BudgetTemplate(pk);
        budgetTemplateRepository.save(template);
    }
}
