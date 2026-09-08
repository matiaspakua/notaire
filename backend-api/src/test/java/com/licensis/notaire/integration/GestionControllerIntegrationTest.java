package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Person;
import com.licensis.notaire.negocio.Suplencia;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.SuplenciaRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.testing.RequirementCoverage;

import java.util.Calendar;
import java.util.Date;

@SpringBootTest
@ActiveProfiles("test-h2")
@RequirementCoverage({"CU02", "CU22"})
@DisplayName("Gestion controller — create validates data before hitting the database")
class GestionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EstadoDeGestionRepository estadoDeGestionRepository;

    @Autowired
    private TipoDeTramiteRepository tipoDeTramiteRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private GestionDeEscrituraRepository gestionDeEscrituraRepository;

    @Autowired
    private PersonRepository personaRepository;

    @Autowired
    private SuplenciaRepository suplenciaRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPersona() throws Exception {
        return createPersona("420" + (System.nanoTime() % 100000));
    }

    private Integer createPersona(String numeroIdentificacion) throws Exception {
        String body = """
                {"firstName": "Escribano IT", "lastName": "Gestion IT", "identificationNumber": "%s",
                 "isClient": false, "tipoIdentificacion": {"idTipoIdentificacion": 1}}
                """.formatted(numeroIdentificacion);
        MvcResult result = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("personId").asInt();
    }

    private Integer createPresupuesto(Integer clienteId) throws Exception {
        String body = """
                {"numero": 1, "fecha": "2026-01-01", "encabezado": "Presupuesto IT", "estado": "PENDIENTE",
                 "persona": {"personId": %d}}
                """.formatted(clienteId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPresupuesto").asInt();
    }

    private Integer createEstadoDeGestion() {
        EstadoDeGestion estado = new EstadoDeGestion();
        estado.setNombre("Estado IT");
        return estadoDeGestionRepository.save(estado).getIdEstadoGestion();
    }

    private Integer createTipoDeTramite() {
        TipoDeTramite tipo = new TipoDeTramite();
        tipo.setNombre("Tramite IT");
        tipo.setHabilitado(true);
        tipo.setSeArchiva(false);
        tipo.setSeInscribe(false);
        tipo.setAsociaInmuebles(false);
        return tipoDeTramiteRepository.save(tipo).getIdTipoTramite();
    }

    @Test
    @DisplayName("Should return 400 with a body, not a bare 500, when encabezado is missing")
    void shouldReturn400WhenEncabezadoIsMissing() throws Exception {
        Integer personaId = createPersona();
        String body = """
                {"fechaInicio": "2026-01-01", "numero": 9101,
                 "fkIdPersonaEscribano": {"personId": %d}}
                """.formatted(personaId);

        mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("Should return 201 when creating a gestion with all required fields")
    void shouldCreateGestionWithValidData() throws Exception {
        Integer personaId = createPersona();
        String body = """
                {"encabezado": "Gestion IT", "fechaInicio": "2026-01-01", "numero": 9102,
                 "fkIdPersonaEscribano": {"personId": %d}}
                """.formatted(personaId);

        mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idGestion").isNumber());
    }

    @Test
    @DisplayName("Should return 400 from complete-case when a required dependency is missing")
    void shouldReturn400FromCompleteCaseWhenDependencyIsMissing() throws Exception {
        String body = """
                {"numero": 9201, "encabezado": "Gestion IT"}
                """;

        mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create a gestion with its tramite when all case dependencies are provided")
    void shouldCreateCompleteCaseWithValidDependencies() throws Exception {
        Integer clienteId = createPersona("42000010");
        Integer escribanoId = createPersona("42000011");
        Integer presupuestoId = createPresupuesto(clienteId);
        Integer estadoId = createEstadoDeGestion();
        Integer tipoTramiteId = createTipoDeTramite();
        String body = """
                {"numero": 9202, "encabezado": "Gestion IT", "presupuestoId": %d,
                 "escribanoId": %d, "estadoGestionId": %d, "tipoTramiteId": %d}
                """.formatted(presupuestoId, escribanoId, estadoId, tipoTramiteId);

        MvcResult result = mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idGestion").isNumber())
                .andReturn();
        Integer gestionId = mapper.readTree(result.getResponse().getContentAsString()).get("idGestion").asInt();

        List<Tramite> tramites = tramiteRepository.findByFkIdGestionIdGestion(gestionId);
        assertThat(tramites).as("complete-case should persist a tramite linked to the gestion").hasSize(1);
        assertThat(tramites.get(0).getFkIdPresupuesto())
                .as("the persisted tramite should carry the requested presupuestoId as its fkIdPresupuesto")
                .isNotNull()
                .extracting("idPresupuesto")
                .isEqualTo(presupuestoId);
    }

    @Test
    @DisplayName("Should return 404 when updating complete-case for a gestion that does not exist")
    void shouldReturn404WhenUpdatingCompleteCaseForMissingGestion() throws Exception {
        Integer clienteId = createPersona("42000012");
        Integer escribanoId = createPersona("42000013");
        Integer presupuestoId = createPresupuesto(clienteId);
        Integer estadoId = createEstadoDeGestion();
        Integer tipoTramiteId = createTipoDeTramite();
        String body = """
                {"numero": 9203, "encabezado": "Gestion IT", "presupuestoId": %d,
                 "escribanoId": %d, "estadoGestionId": %d, "tipoTramiteId": %d}
                """.formatted(presupuestoId, escribanoId, estadoId, tipoTramiteId);

        mockMvc.perform(put("/api/v1/gestiones/999999/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update a gestion and its tramite when all case dependencies are provided")
    void shouldUpdateCompleteCaseWithValidDependencies() throws Exception {
        Integer clienteId = createPersona("42000014");
        Integer escribanoId = createPersona("42000015");
        Integer presupuestoId = createPresupuesto(clienteId);
        Integer estadoId = createEstadoDeGestion();
        Integer tipoTramiteId = createTipoDeTramite();
        String createBody = """
                {"numero": 9204, "encabezado": "Gestion IT", "presupuestoId": %d,
                 "escribanoId": %d, "estadoGestionId": %d, "tipoTramiteId": %d}
                """.formatted(presupuestoId, escribanoId, estadoId, tipoTramiteId);
        MvcResult created = mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer gestionId = mapper.readTree(created.getResponse().getContentAsString()).get("idGestion").asInt();

        String updateBody = """
                {"numero": 9204, "encabezado": "Gestion IT actualizada", "presupuestoId": %d,
                 "escribanoId": %d, "estadoGestionId": %d, "tipoTramiteId": %d}
                """.formatted(presupuestoId, escribanoId, estadoId, tipoTramiteId);

        mockMvc.perform(put("/api/v1/gestiones/" + gestionId + "/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idGestion").value(gestionId));
    }

    @Test
    @DisplayName("Should redirect a gestion to the suplente when the requested escribano has an active suplencia")
    void shouldRedirectToSuplenteWhenUpdatingGestionEscribano() throws Exception {
        Integer clienteId = createPersona("42000016");
        Integer escribanoId = createPersona("42000017");
        Integer suplenteId = createPersona("42000018");
        Integer presupuestoId = createPresupuesto(clienteId);
        Integer estadoId = createEstadoDeGestion();
        Integer tipoTramiteId = createTipoDeTramite();
        String createBody = """
                {"numero": 9205, "encabezado": "Gestion IT", "presupuestoId": %d,
                 "escribanoId": %d, "estadoGestionId": %d, "tipoTramiteId": %d}
                """.formatted(presupuestoId, escribanoId, estadoId, tipoTramiteId);
        MvcResult created = mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer gestionId = mapper.readTree(created.getResponse().getContentAsString()).get("idGestion").asInt();
        createActiveSuplencia(escribanoId, suplenteId);

        String updateBody = """
                {"numero": 9205, "encabezado": "Gestion IT", "presupuestoId": %d,
                 "escribanoId": %d, "estadoGestionId": %d, "tipoTramiteId": %d}
                """.formatted(presupuestoId, escribanoId, estadoId, tipoTramiteId);
        mockMvc.perform(put("/api/v1/gestiones/" + gestionId + "/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        GestionDeEscritura gestion = gestionDeEscrituraRepository.findById(gestionId).orElseThrow();
        Person suplente = personaRepository.findById(suplenteId).orElseThrow();
        assertThat(gestion.getFkIdPersonaEscribano().getPersonId())
                .as("the gestion should be redirected to the suplente, not the requested escribano")
                .isEqualTo(suplenteId);
        assertThat(gestion.getObservaciones())
                .as("the redirection should be recorded, identifying both escribanos")
                .contains(suplente.getFirstName())
                .contains(suplente.getLastName());
    }

    private void createActiveSuplencia(Integer escribanoId, Integer suplenteId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date fechaInicio = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        Date fechaFin = calendar.getTime();

        Suplencia suplencia = new Suplencia(null, fechaInicio, fechaFin);
        suplencia.setFkIdSuplantado(personaRepository.findById(escribanoId).orElseThrow());
        suplencia.setFkIdSuplente(personaRepository.findById(suplenteId).orElseThrow());
        suplenciaRepository.save(suplencia);
    }
}
