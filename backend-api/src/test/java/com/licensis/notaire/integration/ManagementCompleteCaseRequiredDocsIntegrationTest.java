package com.licensis.notaire.integration;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.testing.RequirementCoverage;

@SpringBootTest
@ActiveProfiles("test-h2")
@RequirementCoverage({"CU02", "CU03", "CU72"})
@DisplayName("Complete-case seeds required documents from the procedure template (#773)")
class ManagementCompleteCaseRequiredDocsIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ManagementStatusRepository managementStatusRepository;
    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;
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
                {"firstName": "Notary 773", "lastName": "Docs", "identificationNumber": "%s",
                 "isClient": false, "identificationType": {"idIdentificationType": 1}}
                """.formatted(identificationNumber);
        MvcResult result = mockMvc.perform(post("/api/v1/people")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("personId").asInt();
    }

    private Integer createBudget(Integer clientId) throws Exception {
        String body = """
                {"number": 1, "date": "2026-01-01", "heading": "Budget 773", "status": "PENDIENTE",
                 "person": {"personId": %d}}
                """.formatted(clientId);
        MvcResult result = mockMvc.perform(post("/api/v1/budgets")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    private Integer createStatus() {
        ManagementStatus status = new ManagementStatus();
        status.setName("Status 773");
        return managementStatusRepository.save(status).getIdManagementStatus();
    }

    private ProcedureType createProcedureType(String name) {
        ProcedureType type = new ProcedureType();
        type.setName(name);
        type.setEnabled(true);
        type.setIsArchived(false);
        type.setIsRegistered(false);
        type.setAssociatesProperties(false);
        return procedureTypeRepository.save(type);
    }

    private void addTemplate(ProcedureType type, String documentName) {
        DocumentType documentType = new DocumentType();
        documentType.setName(documentName);
        documentType.setExpires(false);
        documentType.setDeliveredBy("CLIENT");
        documentType.setEnabled(true);
        documentType = documentTypeRepository.save(documentType);
        ProcedureTemplate template = new ProcedureTemplate(type.getIdProcedureType(),
                documentType.getIdDocumentType());
        template.setProcedureType(type);
        template.setDocumentType(documentType);
        procedureTemplateRepository.save(template);
    }

    private String caseBody(int number, Integer budgetId, Integer notaryId, Integer statusId, Integer typeId) {
        return """
                {"number": %d, "heading": "Case 773", "budgetId": %d, "notaryId": %d,
                 "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(number, budgetId, notaryId, statusId, typeId);
    }

    private Integer createCase(String body) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/managements/complete-case")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        return mapper.readTree(created.getResponse().getContentAsString()).get("idManagement").asInt();
    }

    @Test
    @DisplayName("shouldListSeededPendingDocumentsInCaseSummaryAfterCompleteCase")
    void shouldListSeededPendingDocumentsInCaseSummaryAfterCompleteCase() throws Exception {
        ProcedureType type = createProcedureType("Type 773 A");
        addTemplate(type, "Doc 773 one");
        addTemplate(type, "Doc 773 two");
        Integer budgetId = createBudget(createPerson("77300001"));
        Integer notaryId = createPerson("77300002");
        Integer managementId = createCase(caseBody(77301, budgetId, notaryId, createStatus(),
                type.getIdProcedureType()));

        mockMvc.perform(get("/api/v1/managements/{id}/case-summary", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documents.length()").value(2))
                .andExpect(jsonPath("$.documents[0].delivered").value(false))
                .andExpect(jsonPath("$.documents[0].reentered").value(false))
                .andExpect(jsonPath("$.documents[0].released").value(false))
                .andExpect(jsonPath("$.documents[0].observed").value(false))
                .andExpect(jsonPath("$.documents[?(@.typeName=='Doc 773 one')]").exists())
                .andExpect(jsonPath("$.documents[?(@.typeName=='Doc 773 two')]").exists());
    }

    @Test
    @DisplayName("shouldReturnEmptyDocumentsWhenProcedureTypeHasNoTemplate")
    void shouldReturnEmptyDocumentsWhenProcedureTypeHasNoTemplate() throws Exception {
        ProcedureType type = createProcedureType("Type 773 B");
        Integer budgetId = createBudget(createPerson("77300003"));
        Integer notaryId = createPerson("77300004");
        Integer managementId = createCase(caseBody(77302, budgetId, notaryId, createStatus(),
                type.getIdProcedureType()));

        mockMvc.perform(get("/api/v1/managements/{id}/case-summary", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documents.length()").value(0));
    }

    @Test
    @DisplayName("shouldNotReseedWhenPutUpdatesExistingProcedure")
    void shouldNotReseedWhenPutUpdatesExistingProcedure() throws Exception {
        ProcedureType type = createProcedureType("Type 773 C");
        addTemplate(type, "Doc 773 three");
        Integer budgetId = createBudget(createPerson("77300005"));
        Integer notaryId = createPerson("77300006");
        Integer statusId = createStatus();
        String body = caseBody(77303, budgetId, notaryId, statusId, type.getIdProcedureType());
        Integer managementId = createCase(body);

        mockMvc.perform(put("/api/v1/managements/{id}/complete-case", managementId)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/managements/{id}/case-summary", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documents.length()").value(1));
    }
}
