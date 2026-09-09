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
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;
import com.licensis.notaire.repository.ProcedureTemplateRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("TipoDeDocumento — referential integrity checks")
class DocumentTypeReferentialIntegrityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProcedureTemplateRepository procedureTemplateRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return inUse=false when type de documento is not referenced")
    void shouldReturnInUseFalseWhenNotReferenced() throws Exception {
        int id = createTypeDocument("Tipo_unused_" + System.nanoTime());

        mockMvc.perform(get("/api/v1/tipo-de-documento/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("Should return inUse=true when type de documento is referenced by plantilla tramite")
    void shouldReturnInUseTrueWhenReferencedByProcedureTemplate() throws Exception {
        int id = createTypeDocument("Tipo_used_" + System.nanoTime());
        linkToProcedureTemplate(id);

        mockMvc.perform(get("/api/v1/tipo-de-documento/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(true));
    }

    @Test
    @DisplayName("Should return 409 when editing type de documento that is in use")
    void shouldReturn409WhenEditingTypeDocumentInUse() throws Exception {
        int id = createTypeDocument("Tipo_edit_conflict_" + System.nanoTime());
        linkToProcedureTemplate(id);

        String updateBody = """
                {"name": "Name actualizado", "expires": false}
                """;

        mockMvc.perform(put("/api/v1/tipo-de-documento/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should allow editing type de documento that is not in use")
    void shouldAllowEditingTypeDocumentNotInUse() throws Exception {
        int id = createTypeDocument("Tipo_edit_ok_" + System.nanoTime());

        String updateBody = """
                {"name": "Name actualizado ok", "expires": false}
                """;

        mockMvc.perform(put("/api/v1/tipo-de-documento/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 409 when deleting type de documento that is in use")
    void shouldReturn409WhenDeletingTypeDocumentInUse() throws Exception {
        int id = createTypeDocument("Tipo_delete_conflict_" + System.nanoTime());
        linkToProcedureTemplate(id);

        mockMvc.perform(delete("/api/v1/tipo-de-documento/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should search tipos de documento by name")
    void shouldSearchTiposDocumentByName() throws Exception {
        String uniqueName = "SearchableDoc_" + System.nanoTime();
        createTypeDocument(uniqueName);

        mockMvc.perform(get("/api/v1/tipo-de-documento/search")
                        .param("name", "SearchableDoc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name =~ /.*SearchableDoc.*/)]").exists());
    }

    private int createTypeDocument(String name) throws Exception {
        String body = String.format("""
                {"name": "%s", "expires": false}
                """, name);

        MvcResult result = mockMvc.perform(post("/api/v1/tipo-de-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return mapper.readTree(result.getResponse().getContentAsString())
                .get("idDocumentType").asInt();
    }

    private void linkToProcedureTemplate(int idDocumentType) {
        ProcedureTemplatePK pk = new ProcedureTemplatePK(1, idDocumentType);
        ProcedureTemplate template = new ProcedureTemplate(pk);
        procedureTemplateRepository.save(template);
    }
}
