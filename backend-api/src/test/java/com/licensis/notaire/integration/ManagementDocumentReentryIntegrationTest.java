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
 * CU43 - Reenter documentation.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Management reentry of documentation (CU43)")
class ManagementDocumentReentryIntegrationTest {

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

    private record ManagementWithProcedure(Integer idManagement, Integer idProcedure, ProcedureType procedureType) {
    }

    private ManagementWithProcedure createManagementWithProcedure(Integer number) throws Exception {
        Integer notaryId = createPerson("43100" + number);
        String body = """
                {"heading": "Management CU43", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d}
                """.formatted(number, notaryId);
        MvcResult result = mockMvc.perform(post("/api/v1/managements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(result.getResponse().getContentAsString()).get("idManagement").asInt();

        ProcedureType type = new ProcedureType();
        type.setName("Procedure CU43 " + number);
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

        return new ManagementWithProcedure(managementId, procedure.getIdProcedure(), type);
    }

    private DocumentType createDocumentType(String name) {
        DocumentType documentType = new DocumentType();
        documentType.setName(name);
        documentType.setEnabled(true);
        documentType.setReturned(false);
        documentType.setExpires(true);
        documentType.setDueDays(30);
        documentType.setDeliveredBy("Cliente");
        return documentTypeRepository.save(documentType);
    }

    private void createProcedureTemplate(ProcedureType procedureType, DocumentType documentType) {
        ProcedureTemplate template = new ProcedureTemplate(
                new ProcedureTemplatePK(procedureType.getIdProcedureType(), documentType.getIdDocumentType()));
        template.setProcedureType(procedureType);
        template.setDocumentType(documentType);
        procedureTemplateRepository.save(template);
    }

    @Test
    @DisplayName("Should return 404 when the management does not exist (GET)")
    void shouldReturn404OnGetWhenManagementDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/managements/999999/document-reentry"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should list procedures with their documentation required")
    void shouldListProceduresWithRequiredDocumentation() throws Exception {
        ManagementWithProcedure management = createManagementWithProcedure(9401);
        DocumentType documentType = createDocumentType("Certificado de Dominio CU43-1");
        createProcedureTemplate(management.procedureType(), documentType);

        mockMvc.perform(get("/api/v1/managements/" + management.idManagement() + "/document-reentry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idManagement").value(management.idManagement()))
                .andExpect(jsonPath("$.procedures[0].idProcedure").value(management.idProcedure()))
                .andExpect(jsonPath("$.procedures[0].requiredDocuments[0].name")
                        .value("Certificado de Dominio CU43-1"));
    }

    @Test
    @DisplayName("Should return empty documentation required when the procedure has not ProcedureTemplate")
    void shouldReturnEmptyDocumentationWhenNoTemplate() throws Exception {
        ManagementWithProcedure management = createManagementWithProcedure(9402);

        mockMvc.perform(get("/api/v1/managements/" + management.idManagement() + "/document-reentry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.procedures[0].requiredDocuments").isEmpty());
    }

    @Test
    @DisplayName("Should create a SubmittedDocument with reentered=true when the pair is valid")
    void shouldReenterWhenValid() throws Exception {
        ManagementWithProcedure management = createManagementWithProcedure(9403);
        DocumentType documentType = createDocumentType("Certificado de Dominio CU43-3");
        createProcedureTemplate(management.procedureType(), documentType);

        String body = """
                {"idProcedure": %d, "idDocumentType": %d}
                """.formatted(management.idProcedure(), documentType.getIdDocumentType());

        mockMvc.perform(post("/api/v1/managements/" + management.idManagement() + "/document-reentry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProcedure").value(management.idProcedure()))
                .andExpect(jsonPath("$.name").value("Certificado de Dominio CU43-3"))
                .andExpect(jsonPath("$.reentered").value(true));
    }

    @Test
    @DisplayName("Should return 400 when the document type is not part of the ProcedureTemplate")
    void shouldReturn400WhenTypeDocumentNotInTemplate() throws Exception {
        ManagementWithProcedure management = createManagementWithProcedure(9404);
        DocumentType documentType = createDocumentType("Certificado de Dominio CU43-4");

        String body = """
                {"idProcedure": %d, "idDocumentType": %d}
                """.formatted(management.idProcedure(), documentType.getIdDocumentType());

        mockMvc.perform(post("/api/v1/managements/" + management.idManagement() + "/document-reentry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when the procedure does not belong to the management")
    void shouldReturn400WhenProcedureDoesNotBelongToManagement() throws Exception {
        ManagementWithProcedure management = createManagementWithProcedure(9405);
        ManagementWithProcedure otherManagement = createManagementWithProcedure(9406);
        DocumentType documentType = createDocumentType("Certificado de Dominio CU43-5");
        createProcedureTemplate(otherManagement.procedureType(), documentType);

        String body = """
                {"idProcedure": %d, "idDocumentType": %d}
                """.formatted(otherManagement.idProcedure(), documentType.getIdDocumentType());

        mockMvc.perform(post("/api/v1/managements/" + management.idManagement() + "/document-reentry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when the procedure does not exist")
    void shouldReturn404WhenProcedureDoesNotExist() throws Exception {
        ManagementWithProcedure management = createManagementWithProcedure(9407);
        DocumentType documentType = createDocumentType("Certificado de Dominio CU43-6");

        String body = """
                {"idProcedure": 999999, "idDocumentType": %d}
                """.formatted(documentType.getIdDocumentType());

        mockMvc.perform(post("/api/v1/managements/" + management.idManagement() + "/document-reentry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
