package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("DocumentoPresentado controller — create/update/delete via DTO")
class SubmittedDocumentControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private SubmittedDocumentRepository submittedDocumentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createDocumentType(boolean expires, Integer dueDays, String deliveredBy) {
        DocumentType type = new DocumentType();
        type.setName("Tipo DocumentoPresentadoControllerTest " + System.nanoTime());
        type.setExpires(expires);
        type.setDueDays(dueDays);
        type.setDeliveredBy(deliveredBy);
        type.setEnabled(true);
        type = documentTypeRepository.save(type);
        return type.getIdDocumentType();
    }

    private Integer createProcedure() {
        ProcedureType type = new ProcedureType();
        type.setName("Tramite DocumentoPresentadoControllerTest");
        type.setEnabled(true);
        type.setIsArchived(false);
        type.setIsRegistered(false);
        type.setAssociatesProperties(false);
        type = procedureTypeRepository.save(type);

        Procedure procedure = new Procedure();
        procedure.setFkIdProcedureType(type);
        procedure = procedureRepository.save(procedure);
        return procedure.getIdProcedure();
    }

    @Test
    @DisplayName("Should return 201 when creating documento presentado with tipoId, date and delivered")
    void shouldCreateSubmittedDocumentWithDtoFields() throws Exception {
        String body = """
                {"typeId": null, "date": "2024-06-01", "delivered": false}
                """;

        mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idSubmittedDocument").isNumber())
                .andExpect(jsonPath("$.delivered").value(false));
    }

    @Test
    @DisplayName("Should return 201 when creating documento presentado with delivered true")
    void shouldCreateSubmittedDocumentDelivered() throws Exception {
        String body = """
                {"typeId": null, "date": "2024-07-15", "delivered": true}
                """;

        mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.delivered").value(true));
    }

    @Test
    @DisplayName("Should return 201 when creating documento presentado linked to a tramite with deliveredBy")
    void shouldCreateSubmittedDocumentLinkedToProcedure() throws Exception {
        Integer procedureId = createProcedure();
        String body = """
                {"typeId": null, "date": "2024-06-01", "delivered": false, "procedureId": %d,
                 "deliveredBy": "Entidad Externa"}
                """.formatted(procedureId);

        MvcResult result = mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idSubmittedDocument").isNumber())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idSubmittedDocument").asInt();
        var saved = submittedDocumentRepository.findById(id).orElseThrow();
        assertThat(saved.getFkIdProcedure().getIdProcedure()).isEqualTo(procedureId);
        assertThat(saved.getDeliveredBy()).isEqualTo("Entidad Externa");
    }

    @Test
    @DisplayName("Should inherit expires, dueDays and deliveredBy from type de documento and compute dateDue")
    void shouldInheritDueFieldsFromDocumentType() throws Exception {
        Integer typeId = createDocumentType(true, 5, "Escribano");
        String body = """
                {"typeId": %d, "date": "2024-01-01", "delivered": false}
                """.formatted(typeId);

        MvcResult result = mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idSubmittedDocument").asInt();
        var saved = submittedDocumentRepository.findById(id).orElseThrow();
        assertThat(saved.getExpires()).isTrue();
        assertThat(saved.getDueDays()).isEqualTo(5);
        assertThat(saved.getDeliveredBy()).isEqualTo("Escribano");
        assertThat(saved.getDateDue()).isEqualTo(new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-06"));
    }

    @Test
    @DisplayName("Should not compute dateDue when type de documento does not expires")
    void shouldNotComputeDateDueWhenTypeDoesNotExpires() throws Exception {
        Integer typeId = createDocumentType(false, null, "Cliente");
        String body = """
                {"typeId": %d, "date": "2024-01-01", "delivered": false}
                """.formatted(typeId);

        MvcResult result = mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idSubmittedDocument").asInt();
        var saved = submittedDocumentRepository.findById(id).orElseThrow();
        assertThat(saved.getExpires()).isFalse();
        assertThat(saved.getDateDue()).isNull();
        assertThat(saved.getDeliveredBy()).isEqualTo("Cliente");
    }

    @Test
    @DisplayName("Should let an explicit deliveredBy override the value inherited from type de documento")
    void shouldLetExplicitDeliveredByOverrideDocumentType() throws Exception {
        Integer typeId = createDocumentType(false, null, "Escribano");
        String body = """
                {"typeId": %d, "date": "2024-01-01", "delivered": false, "deliveredBy": "Entidad Externa"}
                """.formatted(typeId);

        MvcResult result = mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idSubmittedDocument").asInt();
        var saved = submittedDocumentRepository.findById(id).orElseThrow();
        assertThat(saved.getDeliveredBy()).isEqualTo("Entidad Externa");
    }

    @Test
    @DisplayName("Should return 200 when listing documents presentados")
    void shouldListDocumentsPresentados() throws Exception {
        mockMvc.perform(get("/api/v1/documento-presentado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return 200 and update documento presentado")
    void shouldUpdateSubmittedDocument() throws Exception {
        String createBody = """
                {"typeId": null, "date": "2024-01-01", "delivered": false}
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer id = mapper.readTree(response).get("idSubmittedDocument").asInt();

        String updateBody = """
                {"typeId": null, "date": "2024-12-31", "delivered": true}
                """;

        mockMvc.perform(put("/api/v1/documento-presentado/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent documento presentado")
    void shouldReturn404WhenUpdatingMissingDocument() throws Exception {
        String body = """
                {"typeId": null, "date": "2024-01-01", "delivered": false}
                """;

        mockMvc.perform(put("/api/v1/documento-presentado/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 200 and delete an existing documento presentado")
    void shouldDeleteSubmittedDocument() throws Exception {
        String createBody = """
                {"typeId": null, "date": "2024-05-10", "delivered": false}
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idSubmittedDocument").asInt();

        mockMvc.perform(delete("/api/v1/documento-presentado/" + id))
                .andExpect(status().isOk());
    }
}
