package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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
 * CU43 - Reingresar documentación, against the real Flyway-managed Postgres
 * schema. {@link GestionReingresoDocumentacionIntegrationTest} runs on
 * {@code test-h2}, whose {@code ddl-auto=create} schema does not enforce the
 * NOT NULL columns (e.g. {@code liberado}, {@code observado}) that
 * {@code documentos_presentados} has in production, so it cannot catch a
 * service that forgets to set them.
 */
@SpringBootTest
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("pg-integration")
@DisplayName("Gestion reingreso de documentación contra el esquema real (CU43)")
class ManagementReingresoDocumentacionPgIntegrationTest extends BaseIntegrationTest {

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

    @Test
    @DisplayName("Should create a DocumentoPresentado with reentered=true against the real Postgres schema")
    void shouldReingresarWhenValidAgainstRealSchema() throws Exception {
        Integer notaryId = createPerson("43pg001");
        String managementBody = """
                {"encabezado": "Management CU43 pg", "dateStart": "2026-01-01", "number": 943001,
                 "fkIdNotaryPerson": {"personId": %d}}
                """.formatted(notaryId);
        MvcResult managementResult = mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(managementBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(managementResult.getResponse().getContentAsString())
                .get("idManagement").asInt();

        ProcedureType typeProcedure = new ProcedureType();
        typeProcedure.setName("Tramite CU43 pg");
        typeProcedure.setEnabled(true);
        typeProcedure.setIsArchived(false);
        typeProcedure.setIsRegistered(false);
        typeProcedure.setAssociatesProperties(false);
        typeProcedure = procedureTypeRepository.save(typeProcedure);

        DeedManagement managementRef = new DeedManagement();
        managementRef.setIdManagement(managementId);
        Procedure procedure = new Procedure();
        procedure.setFkIdProcedureType(typeProcedure);
        procedure.setFkIdManagement(managementRef);
        procedure = procedureRepository.save(procedure);

        DocumentType typeDocument = new DocumentType();
        typeDocument.setName("Certificado de Dominio CU43 pg");
        typeDocument.setEnabled(true);
        typeDocument.setReturned(false);
        typeDocument.setExpires(true);
        typeDocument.setDueDays(30);
        typeDocument.setDeliveredBy("Cliente");
        typeDocument = documentTypeRepository.save(typeDocument);

        ProcedureTemplate template = new ProcedureTemplate(
                new ProcedureTemplatePK(typeProcedure.getIdProcedureType(), typeDocument.getIdDocumentType()));
        template.setProcedureType(typeProcedure);
        template.setDocumentType(typeDocument);
        procedureTemplateRepository.save(template);

        String body = """
                {"idProcedure": %d, "idDocumentType": %d}
                """.formatted(procedure.getIdProcedure(), typeDocument.getIdDocumentType());

        mockMvc.perform(post("/api/v1/gestiones/" + managementId + "/reingreso-documentacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProcedure").value(procedure.getIdProcedure()))
                .andExpect(jsonPath("$.reentered").value(true));
    }
}
