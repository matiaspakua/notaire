package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.FolioTypeRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CU06 - Firmar escritura (issue #832). Linking a Folio to an Escritura has no REST endpoint yet
 * (deferred to issue #838), so tests assign it directly through the repository.
 */
@RequirementCoverage({"CU06"})
@DisplayName("EscrituraController#firmar — CU06 integration tests")
class DeedFirmaControllerIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FolioRepository folioRepository;

    @Autowired
    private FolioTypeRepository folioTypeRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createDeed(String status) throws Exception {
        String body = """
                {
                  "number": %d,
                  "body": "Deed de prueba para firma",
                  "status": "%s",
                  "dateDeedrecording": "2026-06-16"
                }
                """.formatted((int) (System.currentTimeMillis() % 1_000_000), status);
        MvcResult result = mockMvc.perform(post("/api/v1/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        return node.get("idDeed").asInt();
    }

    private void assignFolio(int idDeed) {
        IdentificationType identificationType = new IdentificationType();
        identificationType.setName("DNI");
        identificationType = identificationTypeRepository.save(identificationType);

        Person notary = new Person();
        notary.setFirstName("Escribano");
        notary.setLastName("Test");
        notary.setIdentificationNumber(String.valueOf(System.currentTimeMillis() % 100_000_000));
        notary.setIsClient(false);
        notary.setNotaryRegistrationNumber((int) (System.currentTimeMillis() % 10_000));
        notary.setFkIdIdentificationType(identificationType);
        notary = personRepository.save(notary);

        FolioType folioType = new FolioType();
        folioType.setName("Principal");
        folioType.setEnabled(true);
        folioType = folioTypeRepository.save(folioType);

        Folio folio = new Folio();
        folio.setNumber((int) (System.currentTimeMillis() % 1_000_000));
        folio.setYear(2026);
        folio.setStatus("Nuevo");
        folio.setFkIdNotaryPerson(notary);
        folio.setFkIdFolioType(folioType);
        folio.setFkIdDeed(new com.licensis.notaire.business.Deed(idDeed));
        folioRepository.save(folio);
    }

    @Test
    @DisplayName("Should sign a 'Sin Firmar' deed with a folio assigned")
    void shouldSignDeedSinFirmarWithFolioAsignado() throws Exception {
        int idDeed = createDeed("Sin Firmar");
        assignFolio(idDeed);

        mockMvc.perform(post("/api/v1/escrituras/" + idDeed + "/firmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDeed").value(idDeed))
                .andExpect(jsonPath("$.status").value("Firmada"));
    }

    @Test
    @DisplayName("Should reject signing when no folio is assigned")
    void shouldRejectSigningWhenNoFolioAssigned() throws Exception {
        int idDeed = createDeed("Sin Firmar");

        mockMvc.perform(post("/api/v1/escrituras/" + idDeed + "/firmar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject signing an already 'Firmada' deed")
    void shouldRejectSigningAlreadyFirmadaDeed() throws Exception {
        int idDeed = createDeed("Firmada");
        assignFolio(idDeed);

        mockMvc.perform(post("/api/v1/escrituras/" + idDeed + "/firmar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when signing a non-existing deed")
    void shouldReturn404WhenSigningNonExistingDeed() throws Exception {
        mockMvc.perform(post("/api/v1/escrituras/99999/firmar"))
                .andExpect(status().isNotFound());
    }
}
