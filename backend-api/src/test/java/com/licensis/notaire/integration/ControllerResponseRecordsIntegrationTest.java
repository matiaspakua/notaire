package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Notebook;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.FolioTypeRepository;
import com.licensis.notaire.repository.NotebookRepository;
import com.licensis.notaire.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
@DisplayName("CU76 — controllers answer with records, never with JPA entities (#1279)")
class ControllerResponseRecordsIntegrationTest {

    private static final List<String> ENTITY_INTERNALS = List.of("atributos", "dto", "dtoDocument");

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private FolioRepository folioRepository;
    @Autowired
    private FolioTypeRepository folioTypeRepository;
    @Autowired
    private NotebookRepository notebookRepository;
    @Autowired
    private PersonRepository personRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private JsonNode getJson(String url) throws Exception {
        return mapper.readTree(mockMvc.perform(get(url)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private JsonNode postJson(String url, String body) throws Exception {
        var response = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse();
        assertThat(response.getStatus()).as("POST %s -> %s", url, response.getContentAsString()).isEqualTo(201);
        return mapper.readTree(response.getContentAsString());
    }

    private Folio saveFolio(String status) {
        Folio folio = new Folio();
        folio.setNumber(9001);
        folio.setYear(2026);
        folio.setStatus(status);
        folio.setFkIdFolioType(folioTypeRepository.findById(1).orElseThrow());
        folio.setFkIdNotaryPerson(personRepository.findById(1).orElseThrow());
        return folioRepository.save(folio);
    }

    private void assertNoEntityInternals(JsonNode node) {
        ENTITY_INTERNALS.forEach(field -> assertThat(node.has(field)).as("field " + field).isFalse());
    }

    private void assertNotaryRef(JsonNode notary) {
        assertThat(notary.get("personId").asInt()).isEqualTo(1);
        assertThat(notary.has("notaryRegistrationNumber")).isTrue();
        assertThat(notary.get("lastName").asText()).isNotBlank();
        assertThat(notary.get("identificationNumber").asText()).isNotBlank();
        List.of("taxId", "address", "phone", "email", "birthDate")
                .forEach(field -> assertThat(notary.has(field)).as("contact or tax data " + field).isFalse());
    }

    @Test
    @DisplayName("shouldReturnFolioRecordWithSlimReferences")
    void shouldReturnFolioRecordWithSlimReferences() throws Exception {
        Folio folio = saveFolio("Disponible");

        JsonNode body = getJson("/api/v1/folio/" + folio.getIdFolio());

        assertNoEntityInternals(body);
        assertThat(body.get("idFolio").asInt()).isEqualTo(folio.getIdFolio());
        assertThat(body.get("status").asText()).isEqualTo("Disponible");
        assertThat(body.get("fkIdFolioType").get("idFolioType").asInt()).isEqualTo(1);
        assertNotaryRef(body.get("fkIdNotaryPerson"));
    }

    @Test
    @DisplayName("shouldReturnFolioRecordsFromListAndSearch")
    void shouldReturnFolioRecordsFromListAndSearch() throws Exception {
        saveFolio("Disponible");

        JsonNode all = getJson("/api/v1/folio");
        JsonNode found = getJson("/api/v1/folio/search?status=Disponible");

        assertNoEntityInternals(all.get(0));
        assertNotaryRef(found.get(0).get("fkIdNotaryPerson"));
    }

    @Test
    @DisplayName("shouldReturnFolioRecordsFromAuxiliaryProtocol")
    void shouldReturnFolioRecordsFromAuxiliaryProtocol() throws Exception {
        JsonNode body = getJson("/api/v1/protocolo-auxiliar/folios-disponibles");

        assertThat(body.isArray()).isTrue();
        body.forEach(this::assertNoEntityInternals);
    }

    @Test
    @DisplayName("shouldReturnNotebookRecordWithSlimNotary")
    void shouldReturnNotebookRecordWithSlimNotary() throws Exception {
        Notebook notebook = new Notebook();
        notebook.setNumber(77);
        notebook.setYear(2026);
        notebook.setFkIdNotaryPerson(personRepository.findById(1).orElseThrow());
        Notebook saved = notebookRepository.save(notebook);

        JsonNode one = getJson("/api/v1/cuadernos/" + saved.getIdNotebook());
        JsonNode all = getJson("/api/v1/cuadernos");

        assertNoEntityInternals(one);
        assertThat(one.get("idNotebook").asInt()).isEqualTo(saved.getIdNotebook());
        assertNotaryRef(one.get("fkIdNotaryPerson"));
        assertThat(all.isArray()).isTrue();
    }

    @Test
    @DisplayName("shouldReturnAvailableNotariesAsPersonRecords")
    void shouldReturnAvailableNotariesAsPersonRecords() throws Exception {
        JsonNode body = getJson("/api/v1/escrituras/escribanos-disponibles");

        assertThat(body.isArray()).isTrue();
        body.forEach(person -> {
            assertNoEntityInternals(person);
            assertThat(person.has("personId")).isTrue();
        });
    }

    @Test
    @DisplayName("shouldReturnCostTemplateRecordsByProcedureType")
    void shouldReturnCostTemplateRecordsByProcedureType() throws Exception {
        JsonNode type = postJson("/api/v1/tipo-tramite",
                "{\"name\":\"Tipo costos\",\"isRegistered\":false,\"isArchived\":false,"
                        + "\"associatesProperties\":false}");
        JsonNode document = postJson("/api/v1/tipo-de-documento",
                "{\"name\":\"Doc costos\",\"expires\":false,\"deliveredBy\":\"Cliente\",\"returned\":false}");
        postJson("/api/v1/plantilla-costos-documento", "{\"idProcedureType\":" + type.get("idProcedureType")
                + ",\"idDocumentType\":" + document.get("idDocumentType")
                + ",\"fixedAmount\":10}");

        JsonNode body = getJson("/api/v1/plantilla-costos-documento/tipo-tramite/" + type.get("idProcedureType"));

        JsonNode first = body.get(0);
        assertNoEntityInternals(first);
        assertThat(first.get("documentCostTemplatePK").get("fkIdDocumentType").asInt())
                .isEqualTo(document.get("idDocumentType").asInt());
        assertThat(first.get("procedureType").get("name").asText()).isEqualTo("Tipo costos");
        assertThat(first.get("documentType").get("name").asText()).isEqualTo("Doc costos");
        assertThat(first.get("fixedAmount").decimalValue()).isEqualByComparingTo("10");
    }

    @Test
    @DisplayName("shouldReturnProcedureRecordsFromPageAndById")
    void shouldReturnProcedureRecordsFromPageAndById() throws Exception {
        JsonNode type = postJson("/api/v1/tipo-tramite",
                "{\"name\":\"Tipo tramite\",\"isRegistered\":false,\"isArchived\":false,"
                        + "\"associatesProperties\":false}");
        JsonNode procedure = postJson("/api/v1/tramites", "{\"notes\":\"n\",\"idProcedureType\":"
                + type.get("idProcedureType") + "}");

        JsonNode one = getJson("/api/v1/tramites/" + procedure.get("idProcedure"));
        JsonNode page = getJson("/api/v1/tramites");

        assertNoEntityInternals(one);
        assertThat(one.get("fkIdProcedureType").get("idProcedureType").asInt())
                .isEqualTo(type.get("idProcedureType").asInt());
        assertThat(page.get("content").get(0).has("fkIdProcedureType")).isTrue();
        assertThat(page.has("totalElements")).isTrue();
    }
}
