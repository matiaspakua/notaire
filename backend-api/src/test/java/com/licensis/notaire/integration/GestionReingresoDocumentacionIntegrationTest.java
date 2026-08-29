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
 * CU43 - Reingresar documentación.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Gestion reingreso de documentación (CU43)")
class GestionReingresoDocumentacionIntegrationTest {

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

    private record GestionConTramite(Integer idGestion, Integer idTramite, TipoDeTramite tipoDeTramite) {
    }

    private GestionConTramite createGestionConTramite(Integer numero) throws Exception {
        Integer escribanoId = createPersona("43100" + numero);
        String body = """
                {"encabezado": "Gestion CU43", "fechaInicio": "2026-01-01", "numero": %d,
                 "fkIdPersonaEscribano": {"idPersona": %d}}
                """.formatted(numero, escribanoId);
        MvcResult result = mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer gestionId = mapper.readTree(result.getResponse().getContentAsString()).get("idGestion").asInt();

        TipoDeTramite tipo = new TipoDeTramite();
        tipo.setNombre("Tramite CU43 " + numero);
        tipo.setHabilitado(true);
        tipo.setSeArchiva(false);
        tipo.setSeInscribe(false);
        tipo.setAsociaInmuebles(false);
        tipo = tipoDeTramiteRepository.save(tipo);

        GestionDeEscritura gestionRef = new GestionDeEscritura();
        gestionRef.setIdGestion(gestionId);

        Tramite tramite = new Tramite();
        tramite.setFkIdTipoTramite(tipo);
        tramite.setFkIdGestion(gestionRef);
        tramite = tramiteRepository.save(tramite);

        return new GestionConTramite(gestionId, tramite.getIdTramite(), tipo);
    }

    private TipoDeDocumento createTipoDeDocumento(String nombre) {
        TipoDeDocumento tipoDocumento = new TipoDeDocumento();
        tipoDocumento.setNombre(nombre);
        tipoDocumento.setHabilitado(true);
        tipoDocumento.setDevuelto(false);
        tipoDocumento.setVence(true);
        tipoDocumento.setDiasVencimiento(30);
        tipoDocumento.setQuienEntrega("Cliente");
        return tipoDeDocumentoRepository.save(tipoDocumento);
    }

    private void createPlantillaTramite(TipoDeTramite tipoTramite, TipoDeDocumento tipoDocumento) {
        PlantillaTramite plantilla = new PlantillaTramite(
                new PlantillaTramitePK(tipoTramite.getIdTipoTramite(), tipoDocumento.getIdTipoDocumento()));
        plantilla.setTipoDeTramite(tipoTramite);
        plantilla.setTipoDeDocumento(tipoDocumento);
        plantillaTramiteRepository.save(plantilla);
    }

    @Test
    @DisplayName("Should return 404 when the gestión does not exist (GET)")
    void shouldReturn404OnGetWhenGestionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/999999/reingreso-documentacion"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should list trámites with their documentación necesaria")
    void shouldListTramitesWithDocumentacionNecesaria() throws Exception {
        GestionConTramite gestion = createGestionConTramite(9401);
        TipoDeDocumento tipoDocumento = createTipoDeDocumento("Certificado de Dominio CU43-1");
        createPlantillaTramite(gestion.tipoDeTramite(), tipoDocumento);

        mockMvc.perform(get("/api/v1/gestiones/" + gestion.idGestion() + "/reingreso-documentacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idGestion").value(gestion.idGestion()))
                .andExpect(jsonPath("$.tramites[0].idTramite").value(gestion.idTramite()))
                .andExpect(jsonPath("$.tramites[0].documentosNecesarios[0].nombre")
                        .value("Certificado de Dominio CU43-1"));
    }

    @Test
    @DisplayName("Should return empty documentación necesaria when the trámite has no PlantillaTramite")
    void shouldReturnEmptyDocumentacionWhenNoPlantilla() throws Exception {
        GestionConTramite gestion = createGestionConTramite(9402);

        mockMvc.perform(get("/api/v1/gestiones/" + gestion.idGestion() + "/reingreso-documentacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tramites[0].documentosNecesarios").isEmpty());
    }

    @Test
    @DisplayName("Should create a DocumentoPresentado with reingresado=true when the pair is valid")
    void shouldReingresarWhenValid() throws Exception {
        GestionConTramite gestion = createGestionConTramite(9403);
        TipoDeDocumento tipoDocumento = createTipoDeDocumento("Certificado de Dominio CU43-3");
        createPlantillaTramite(gestion.tipoDeTramite(), tipoDocumento);

        String body = """
                {"idTramite": %d, "idTipoDocumento": %d}
                """.formatted(gestion.idTramite(), tipoDocumento.getIdTipoDocumento());

        mockMvc.perform(post("/api/v1/gestiones/" + gestion.idGestion() + "/reingreso-documentacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTramite").value(gestion.idTramite()))
                .andExpect(jsonPath("$.nombre").value("Certificado de Dominio CU43-3"))
                .andExpect(jsonPath("$.reingresado").value(true));
    }

    @Test
    @DisplayName("Should return 400 when the tipo de documento is not part of the PlantillaTramite")
    void shouldReturn400WhenTipoDocumentoNotInPlantilla() throws Exception {
        GestionConTramite gestion = createGestionConTramite(9404);
        TipoDeDocumento tipoDocumento = createTipoDeDocumento("Certificado de Dominio CU43-4");

        String body = """
                {"idTramite": %d, "idTipoDocumento": %d}
                """.formatted(gestion.idTramite(), tipoDocumento.getIdTipoDocumento());

        mockMvc.perform(post("/api/v1/gestiones/" + gestion.idGestion() + "/reingreso-documentacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when the trámite does not belong to the gestión")
    void shouldReturn400WhenTramiteDoesNotBelongToGestion() throws Exception {
        GestionConTramite gestion = createGestionConTramite(9405);
        GestionConTramite otraGestion = createGestionConTramite(9406);
        TipoDeDocumento tipoDocumento = createTipoDeDocumento("Certificado de Dominio CU43-5");
        createPlantillaTramite(otraGestion.tipoDeTramite(), tipoDocumento);

        String body = """
                {"idTramite": %d, "idTipoDocumento": %d}
                """.formatted(otraGestion.idTramite(), tipoDocumento.getIdTipoDocumento());

        mockMvc.perform(post("/api/v1/gestiones/" + gestion.idGestion() + "/reingreso-documentacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when the trámite does not exist")
    void shouldReturn404WhenTramiteDoesNotExist() throws Exception {
        GestionConTramite gestion = createGestionConTramite(9407);
        TipoDeDocumento tipoDocumento = createTipoDeDocumento("Certificado de Dominio CU43-6");

        String body = """
                {"idTramite": 999999, "idTipoDocumento": %d}
                """.formatted(tipoDocumento.getIdTipoDocumento());

        mockMvc.perform(post("/api/v1/gestiones/" + gestion.idGestion() + "/reingreso-documentacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
