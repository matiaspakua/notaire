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
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.repository.PropertyRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;

/**
 * CU10 - Registrar movimientos de documentación de entidades externas.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Gestion documents de entidades externas (CU10)")
class ManagementDocumentsEntidadesExternasIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private SubmittedDocumentRepository submittedDocumentRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPerson(String identificationNumber) throws Exception {
        String body = """
                {"firstName": "Notary IT", "lastName": "CU10", "identificationNumber": "%s",
                 "isClient": false, "identificationType": {"idIdentificationType": 1}}
                """.formatted(identificationNumber);
        MvcResult result = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("personId").asInt();
    }

    private Integer createManagementConProcedure(Integer number) throws Exception {
        Integer notaryId = createPerson("42100" + number);
        String body = """
                {"encabezado": "Management CU10", "dateStart": "2026-01-01", "number": %d,
                 "fkIdNotaryPerson": {"personId": %d}}
                """.formatted(number, notaryId);
        MvcResult result = mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(result.getResponse().getContentAsString()).get("idManagement").asInt();

        ProcedureType type = new ProcedureType();
        type.setName("Tramite CU10 " + number);
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
        procedureRepository.save(procedure);

        return managementId;
    }

    private SubmittedDocument createDocumentEntidadExterna(Integer idManagement, String name) {
        Procedure procedure = procedureRepository.findByFkIdManagementIdManagement(idManagement).get(0);
        SubmittedDocument document = new SubmittedDocument();
        document.setName(name);
        document.setDeliveredBy(BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA);
        document.setFkIdProcedure(procedure);
        document.setDelivered(false);
        document.setPrepared(false);
        document.setExpires(false);
        return submittedDocumentRepository.save(document);
    }

    @Test
    @DisplayName("Should return 404 when the gestión does not exist")
    void shouldReturn404WhenManagementDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/999999/documentos-entidades-externas"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should list the entidad externa documents and nomenclatura catastral of a gestión")
    void shouldListDocumentsEntidadesExternas() throws Exception {
        Integer managementId = createManagementConProcedure(9301);
        createDocumentEntidadExterna(managementId, "Certificado de Dominio");

        Property property = new Property();
        property.setAddress("Calle Falsa 123");
        property.setCadastralDesignation("11-22-33");
        property = propertyRepository.save(property);
        Procedure procedure = procedureRepository.findByFkIdManagementIdManagement(managementId).get(0);
        procedure.setFkIdProperty(property);
        procedure = procedureRepository.save(procedure);

        try {
            mockMvc.perform(get("/api/v1/gestiones/" + managementId + "/documentos-entidades-externas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idManagement").value(managementId))
                    .andExpect(jsonPath("$.cadastralDesignation").value("11-22-33"))
                    .andExpect(jsonPath("$.documents[0].name").value("Certificado de Dominio"));
        } finally {
            procedure.setFkIdProperty(null);
            procedureRepository.save(procedure);
            propertyRepository.delete(property);
        }
    }

    @Test
    @DisplayName("Should register the movement of a document and return it updated")
    void shouldRegistrarMovement() throws Exception {
        Integer managementId = createManagementConProcedure(9302);
        SubmittedDocument document = createDocumentEntidadExterna(managementId, "Informe de Dominio");

        String body = """
                {"prepared": true, "cardNumber": 5, "notes": "Retirado", "delivered": false}
                """;

        mockMvc.perform(put("/api/v1/gestiones/" + managementId + "/documentos-entidades-externas/"
                        + document.getIdSubmittedDocument())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prepared").value(true))
                .andExpect(jsonPath("$.cardNumber").value(5))
                .andExpect(jsonPath("$.notes").value("Retirado"));
    }

    @Test
    @DisplayName("Should return 400 when the document does not belong to the gestión")
    void shouldReturn400WhenDocumentDoesNotBelongToManagement() throws Exception {
        Integer managementId = createManagementConProcedure(9303);
        Integer otraManagementId = createManagementConProcedure(9304);
        SubmittedDocument document = createDocumentEntidadExterna(otraManagementId, "Informe de Dominio");

        String body = """
                {"delivered": true}
                """;

        mockMvc.perform(put("/api/v1/gestiones/" + managementId + "/documentos-entidades-externas/"
                        + document.getIdSubmittedDocument())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when the document does not exist")
    void shouldReturn404WhenDocumentDoesNotExist() throws Exception {
        Integer managementId = createManagementConProcedure(9305);

        String body = """
                {"delivered": true}
                """;

        mockMvc.perform(put("/api/v1/gestiones/" + managementId + "/documentos-entidades-externas/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should keep gestión open when the workflow does not define the completion transition")
    void shouldMarkAllDocumentsDeliveredWithoutFailingWhenNoWorkflow() throws Exception {
        Integer managementId = createManagementConProcedure(9306);
        SubmittedDocument document = createDocumentEntidadExterna(managementId, "Libre de Deuda");

        String body = """
                {"delivered": true}
                """;

        mockMvc.perform(put("/api/v1/gestiones/" + managementId + "/documentos-entidades-externas/"
                        + document.getIdSubmittedDocument())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.delivered").value(true));
    }
}
