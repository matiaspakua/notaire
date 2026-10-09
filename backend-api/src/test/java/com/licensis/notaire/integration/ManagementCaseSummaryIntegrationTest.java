package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.testing.RequirementCoverage;
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

/** Issue #774 — GET /api/v1/gestiones/{id}/resumen-caso over the H2 schema. */
@RequirementCoverage({"CU07", "CU11", "CU12", "CU70"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Resumen del caso de una gestión — integration tests")
class ManagementCaseSummaryIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private com.licensis.notaire.repository.DocumentTypeRepository documentTypeRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int idFrom(MvcResult result, String field) throws Exception {
        return mapper.readTree(result.getResponse().getContentAsString()).get(field).asInt();
    }

    private int createManagement(int number) throws Exception {
        MvcResult person = mockMvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Notary IT", "lastName": "CasoResumen", "identificationNumber": "774%d",
                                 "isClient": false, "identificationType": {"idIdentificationType": 1}}
                                """.formatted(number)))
                .andExpect(status().isCreated()).andReturn();
        MvcResult management = mockMvc.perform(post("/api/v1/gestiones").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"encabezado": "Gestión resumen caso", "dateStart": "2026-01-01", "number": %d,
                                 "notaryPersonId": %d}
                                """.formatted(number, idFrom(person, "personId"))))
                .andExpect(status().isCreated()).andReturn();
        return idFrom(management, "idManagement");
    }

    private Procedure createProcedure(int idManagement, Deed deed) {
        ProcedureType type = new ProcedureType();
        type.setName("Tramite caso resumen " + System.nanoTime());
        type.setEnabled(true);
        type.setIsArchived(false);
        type.setIsRegistered(false);
        type.setAssociatesProperties(false);
        type = procedureTypeRepository.save(type);

        DeedManagement managementRef = new DeedManagement();
        managementRef.setIdManagement(idManagement);
        Procedure procedure = new Procedure();
        procedure.setFkIdProcedureType(type);
        procedure.setFkIdManagement(managementRef);
        procedure.setFkIdDeed(deed);
        return procedureRepository.save(procedure);
    }

    private int createSignedDeedWithTestimony(int number) throws Exception {
        MvcResult deed = mockMvc.perform(post("/api/v1/escrituras").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number": %d, "body": "Escritura resumen caso", "status": "Firmada",
                                 "dateDeedrecording": "2026-06-16"}
                                """.formatted(number)))
                .andExpect(status().isCreated()).andReturn();
        int idDeed = idFrom(deed, "idDeed");
        mockMvc.perform(post("/api/v1/testimonio/" + idDeed + "/generar")).andExpect(status().isCreated());
        return idDeed;
    }

    @Test
    @DisplayName("Should return the header and an empty deeds list when no trámite has a deed")
    void shouldReturnEmptyDeedsList() throws Exception {
        int idManagement = createManagement((int) (System.nanoTime() % 900_000) + 100_000);
        createProcedure(idManagement, null);

        mockMvc.perform(get("/api/v1/gestiones/" + idManagement + "/resumen-caso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managementId").value(idManagement))
                .andExpect(jsonPath("$.deeds").isEmpty());
    }

    @Test
    @DisplayName("Should list the deed of a trámite with its generated testimony in state SIN_INGRESAR")
    void shouldListDeedWithTestimony() throws Exception {
        int seed = (int) (System.nanoTime() % 900_000) + 100_000;
        int idManagement = createManagement(seed);
        int idDeed = createSignedDeedWithTestimony(seed);
        Deed deed = new Deed(idDeed);
        createProcedure(idManagement, deed);

        mockMvc.perform(get("/api/v1/gestiones/" + idManagement + "/resumen-caso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deeds[0].idDeed").value(idDeed))
                .andExpect(jsonPath("$.deeds[0].testimonies[0].state").value("SIN_INGRESAR"))
                .andExpect(jsonPath("$.deeds[0].testimonies[0].copies").value(0));
    }

    @Test
    @DisplayName("Should list a document registered against a trámite of the gestión")
    void shouldListDocumentOfTheManagement() throws Exception {
        int idManagement = createManagement((int) (System.nanoTime() % 900_000) + 100_000);
        Procedure procedure = createProcedure(idManagement, null);
        com.licensis.notaire.business.DocumentType type = new com.licensis.notaire.business.DocumentType();
        type.setName("Tipo resumen caso " + System.nanoTime());
        type.setDeliveredBy("Cliente");
        type.setExpires(false);
        type.setEnabled(true);
        type = documentTypeRepository.save(type);
        mockMvc.perform(post("/api/v1/documento-presentado").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"typeId": %d, "date": "2026-06-01", "delivered": false, "procedureId": %d,
                                 "name": "Doc del caso"}
                                """.formatted(type.getIdDocumentType(), procedure.getIdProcedure())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/gestiones/" + idManagement + "/resumen-caso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documents[0].name").value("Doc del caso"))
                .andExpect(jsonPath("$.documents[0].idProcedure").value(procedure.getIdProcedure()));
    }

    @Test
    @DisplayName("Should return 404 for an unknown gestión")
    void shouldReturn404ForUnknownManagement() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/999999/resumen-caso")).andExpect(status().isNotFound());
    }
}
