package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;

/**
 * CU43 - Reingresar documentación.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Gestion reingreso de documentación (CU43)")
class ManagementReingresoDocumentacionIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private ProcedureTemplateRepository procedureTemplateRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPerson(String identificationNumber) throws Exception {
        String body = """
                {"firstName": "Notary IT", "lastName": "CU43", "identificationNumber": "%s",
                 "isClient": false, "identificationType": {"idIdentificationType": 1}}
                """.formatted(identificationNumber);
        MvcResult result = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("personId").asInt();
    }

    private record ManagementConProcedure(Integer idManagement, Integer idProcedure, ProcedureType procedureType) {
    }

    private ManagementConProcedure createManagementConProcedure(Integer number) throws Exception {
        Integer notaryId = createPerson("43100" + number);
        String body = """
                {"encabezado": "Management CU43", "dateStart": "2026-01-01", "number": %d,
                 "fkIdNotaryPerson": {"personId": %d}}
                """.formatted(number, notaryId);
        MvcResult result = mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(result.getResponse().getContentAsString()).get("idManagement").asInt();

        ProcedureType type = new ProcedureType();
        type.setName("Tramite CU43 " + number);
        type.setEnabled(true);
        type.setIsArchived(false);
        type.setIsRegistered(false);
        type.setAssociatesProperties(false);
        type = procedureTypeRepository.save(type);

        DeedManagement managementRef = new DeedManagement();
        managementRef.setIdManagement(managementId);

        Procedure procedure = new Procedure();
        procedure.setFkIdProcedureType(type);
        procedure.setFkIdManagement(managementRef);
        procedure = procedureRepository.save(procedure);

        return new ManagementConProcedure(managementId, procedure.getIdProcedure(), type);
    }

    private DocumentType createDocumentType(String name) {
        DocumentType typeDocument = new DocumentType();
        typeDocument.setName(name);
        typeDocument.setEnabled(true);
        typeDocument.setReturned(false);
        typeDocument.setExpires(true);
        typeDocument.setDueDays(30);
        typeDocument.setDeliveredBy("Cliente");
        return documentTypeRepository.save(typeDocument);
    }

    private void createProcedureTemplate(ProcedureType typeProcedure, DocumentType typeDocument) {
        ProcedureTemplate template = new ProcedureTemplate(
                new ProcedureTemplatePK(typeProcedure.getIdProcedureType(), typeDocument.getIdDocumentType()));
        template.setProcedureType(typeProcedure);
        template.setDocumentType(typeDocument);
        procedureTemplateRepository.save(template);
    }

    @Test
    @DisplayName("Should return 404 when the gestión does not exist (GET)")
    void shouldReturn404OnGetWhenManagementDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/999999/reingreso-documentacion"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should list trámites with their documentación necesaria")
    void shouldListProceduresWithDocumentacionNecesaria() throws Exception {
        ManagementConProcedure management = createManagementConProcedure(9401);
        DocumentType typeDocument = createDocumentType("Certificado de Dominio CU43-1");
        createProcedureTemplate(management.procedureType(), typeDocument);

        mockMvc.perform(get("/api/v1/gestiones/" + management.idManagement() + "/reingreso-documentacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idManagement").value(management.idManagement()))
                .andExpect(jsonPath("$.procedures[0].idProcedure").value(management.idProcedure()))
                .andExpect(jsonPath("$.procedures[0].documentsNecesarios[0].name")
                        .value("Certificado de Dominio CU43-1"));
    }

    @Test
    @DisplayName("Should return empty documentación necesaria when the trámite has no PlantillaTramite")
    void shouldReturnEmptyDocumentacionWhenNoTemplate() throws Exception {
        ManagementConProcedure management = createManagementConProcedure(9402);

        mockMvc.perform(get("/api/v1/gestiones/" + management.idManagement() + "/reingreso-documentacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.procedures[0].documentsNecesarios").isEmpty());
    }

    @Test
    @DisplayName("Should create a DocumentoPresentado with reentered=true when the pair is valid")
    void shouldReingresarWhenValid() throws Exception {
        ManagementConProcedure management = createManagementConProcedure(9403);
        DocumentType typeDocument = createDocumentType("Certificado de Dominio CU43-3");
        createProcedureTemplate(management.procedureType(), typeDocument);

        String body = """
                {"idProcedure": %d, "idDocumentType": %d}
                """.formatted(management.idProcedure(), typeDocument.getIdDocumentType());

        mockMvc.perform(post("/api/v1/gestiones/" + management.idManagement() + "/reingreso-documentacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProcedure").value(management.idProcedure()))
                .andExpect(jsonPath("$.name").value("Certificado de Dominio CU43-3"))
                .andExpect(jsonPath("$.reentered").value(true));
    }

    @Test
    @DisplayName("Should return 400 when the type de documento is not part of the PlantillaTramite")
    void shouldReturn400WhenTypeDocumentNotInTemplate() throws Exception {
        ManagementConProcedure management = createManagementConProcedure(9404);
        DocumentType typeDocument = createDocumentType("Certificado de Dominio CU43-4");

        String body = """
                {"idProcedure": %d, "idDocumentType": %d}
                """.formatted(management.idProcedure(), typeDocument.getIdDocumentType());

        mockMvc.perform(post("/api/v1/gestiones/" + management.idManagement() + "/reingreso-documentacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when the trámite does not belong to the gestión")
    void shouldReturn400WhenProcedureDoesNotBelongToManagement() throws Exception {
        ManagementConProcedure management = createManagementConProcedure(9405);
        ManagementConProcedure otraManagement = createManagementConProcedure(9406);
        DocumentType typeDocument = createDocumentType("Certificado de Dominio CU43-5");
        createProcedureTemplate(otraManagement.procedureType(), typeDocument);

        String body = """
                {"idProcedure": %d, "idDocumentType": %d}
                """.formatted(otraManagement.idProcedure(), typeDocument.getIdDocumentType());

        mockMvc.perform(post("/api/v1/gestiones/" + management.idManagement() + "/reingreso-documentacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when the trámite does not exist")
    void shouldReturn404WhenProcedureDoesNotExist() throws Exception {
        ManagementConProcedure management = createManagementConProcedure(9407);
        DocumentType typeDocument = createDocumentType("Certificado de Dominio CU43-6");

        String body = """
                {"idProcedure": 999999, "idDocumentType": %d}
                """.formatted(typeDocument.getIdDocumentType());

        mockMvc.perform(post("/api/v1/gestiones/" + management.idManagement() + "/reingreso-documentacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
