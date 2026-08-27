package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoDeFolio;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoDeFolioRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
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
class EscrituraFirmaControllerIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FolioRepository folioRepository;

    @Autowired
    private TipoDeFolioRepository tipoDeFolioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createEscritura(String estado) throws Exception {
        String body = """
                {
                  "numero": %d,
                  "cuerpo": "Escritura de prueba para firma",
                  "estado": "%s",
                  "fechaEscrituracion": "2026-06-16"
                }
                """.formatted((int) (System.currentTimeMillis() % 1_000_000), estado);
        MvcResult result = mockMvc.perform(post("/api/v1/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        return node.get("idEscritura").asInt();
    }

    private void assignFolio(int idEscritura) {
        TipoIdentificacion tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setNombre("DNI");
        tipoIdentificacion = tipoIdentificacionRepository.save(tipoIdentificacion);

        Persona escribano = new Persona();
        escribano.setNombre("Escribano");
        escribano.setApellido("Test");
        escribano.setNumeroIdentificacion(String.valueOf(System.currentTimeMillis() % 100_000_000));
        escribano.setEsCliente(false);
        escribano.setRegistroEscribano((int) (System.currentTimeMillis() % 10_000));
        escribano.setFkIdTipoIdentificacion(tipoIdentificacion);
        escribano = personaRepository.save(escribano);

        TipoDeFolio tipoDeFolio = new TipoDeFolio();
        tipoDeFolio.setNombre("Principal");
        tipoDeFolio.setHabilitado(true);
        tipoDeFolio = tipoDeFolioRepository.save(tipoDeFolio);

        Folio folio = new Folio();
        folio.setNumero((int) (System.currentTimeMillis() % 1_000_000));
        folio.setAnio(2026);
        folio.setEstado("Nuevo");
        folio.setFkIdPersonaEscribano(escribano);
        folio.setFkIdTipoFolio(tipoDeFolio);
        folio.setFkIdEscritura(new com.licensis.notaire.negocio.Escritura(idEscritura));
        folioRepository.save(folio);
    }

    @Test
    @DisplayName("Should sign a 'Sin Firmar' escritura with a folio assigned")
    void shouldSignEscrituraSinFirmarWithFolioAsignado() throws Exception {
        int idEscritura = createEscritura("Sin Firmar");
        assignFolio(idEscritura);

        mockMvc.perform(post("/api/v1/escrituras/" + idEscritura + "/firmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEscritura").value(idEscritura))
                .andExpect(jsonPath("$.estado").value("Firmada"));
    }

    @Test
    @DisplayName("Should reject signing when no folio is assigned")
    void shouldRejectSigningWhenNoFolioAssigned() throws Exception {
        int idEscritura = createEscritura("Sin Firmar");

        mockMvc.perform(post("/api/v1/escrituras/" + idEscritura + "/firmar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject signing an already 'Firmada' escritura")
    void shouldRejectSigningAlreadyFirmadaEscritura() throws Exception {
        int idEscritura = createEscritura("Firmada");
        assignFolio(idEscritura);

        mockMvc.perform(post("/api/v1/escrituras/" + idEscritura + "/firmar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when signing a non-existing escritura")
    void shouldReturn404WhenSigningNonExistingEscritura() throws Exception {
        mockMvc.perform(post("/api/v1/escrituras/99999/firmar"))
                .andExpect(status().isNotFound());
    }
}
