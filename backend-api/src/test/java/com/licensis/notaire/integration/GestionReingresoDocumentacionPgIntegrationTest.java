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
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.negocio.PlantillaTramitePK;
import com.licensis.notaire.negocio.TipoDeDocumento;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.PlantillaTramiteRepository;
import com.licensis.notaire.repository.TipoDeDocumentoRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import com.licensis.notaire.repository.TramiteRepository;

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
class GestionReingresoDocumentacionPgIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TipoDeTramiteRepository tipoDeTramiteRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private TipoDeDocumentoRepository tipoDeDocumentoRepository;

    @Autowired
    private PlantillaTramiteRepository plantillaTramiteRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPersona(String numeroIdentificacion) throws Exception {
        String body = """
                {"nombre": "Escribano IT", "apellido": "CU43", "numeroIdentificacion": "%s",
                 "esCliente": false, "tipoIdentificacion": {"idTipoIdentificacion": 1}}
                """.formatted(numeroIdentificacion);
        MvcResult result = mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPersona").asInt();
    }

    @Test
    @DisplayName("Should create a DocumentoPresentado with reingresado=true against the real Postgres schema")
    void shouldReingresarWhenValidAgainstRealSchema() throws Exception {
        Integer escribanoId = createPersona("43pg001");
        String gestionBody = """
                {"encabezado": "Gestion CU43 pg", "fechaInicio": "2026-01-01", "numero": 943001,
                 "fkIdPersonaEscribano": {"idPersona": %d}}
                """.formatted(escribanoId);
        MvcResult gestionResult = mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gestionBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer gestionId = mapper.readTree(gestionResult.getResponse().getContentAsString())
                .get("idGestion").asInt();

        TipoDeTramite tipoTramite = new TipoDeTramite();
        tipoTramite.setNombre("Tramite CU43 pg");
        tipoTramite.setHabilitado(true);
        tipoTramite.setSeArchiva(false);
        tipoTramite.setSeInscribe(false);
        tipoTramite.setAsociaInmuebles(false);
        tipoTramite = tipoDeTramiteRepository.save(tipoTramite);

        GestionDeEscritura gestionRef = new GestionDeEscritura();
        gestionRef.setIdGestion(gestionId);
        Tramite tramite = new Tramite();
        tramite.setFkIdTipoTramite(tipoTramite);
        tramite.setFkIdGestion(gestionRef);
        tramite = tramiteRepository.save(tramite);

        TipoDeDocumento tipoDocumento = new TipoDeDocumento();
        tipoDocumento.setNombre("Certificado de Dominio CU43 pg");
        tipoDocumento.setHabilitado(true);
        tipoDocumento.setDevuelto(false);
        tipoDocumento.setVence(true);
        tipoDocumento.setDiasVencimiento(30);
        tipoDocumento.setQuienEntrega("Cliente");
        tipoDocumento = tipoDeDocumentoRepository.save(tipoDocumento);

        PlantillaTramite plantilla = new PlantillaTramite(
                new PlantillaTramitePK(tipoTramite.getIdTipoTramite(), tipoDocumento.getIdTipoDocumento()));
        plantilla.setTipoDeTramite(tipoTramite);
        plantilla.setTipoDeDocumento(tipoDocumento);
        plantillaTramiteRepository.save(plantilla);

        String body = """
                {"idTramite": %d, "idTipoDocumento": %d}
                """.formatted(tramite.getIdTramite(), tipoDocumento.getIdTipoDocumento());

        mockMvc.perform(post("/api/v1/gestiones/" + gestionId + "/reingreso-documentacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTramite").value(tramite.getIdTramite()))
                .andExpect(jsonPath("$.reingresado").value(true));
    }
}
