package com.licensis.notaire.integration;

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
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Gestion controller — create validates data before hitting the database")
class GestionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EstadoDeGestionRepository estadoDeGestionRepository;

    @Autowired
    private TipoDeTramiteRepository tipoDeTramiteRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPersona() throws Exception {
        return createPersona("42000001");
    }

    private Integer createPersona(String numeroIdentificacion) throws Exception {
        String body = """
                {"nombre": "Escribano IT", "apellido": "Gestion IT", "numeroIdentificacion": "%s",
                 "esCliente": false, "tipoIdentificacion": {"idTipoIdentificacion": 1}}
                """.formatted(numeroIdentificacion);
        MvcResult result = mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPersona").asInt();
    }

    private Integer createPresupuesto(Integer clienteId) throws Exception {
        String body = """
                {"numero": 1, "fecha": "2026-01-01", "encabezado": "Presupuesto IT", "estado": "PENDIENTE",
                 "fkIdPersona": {"idPersona": %d}}
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
                 "fkIdPersonaEscribano": {"idPersona": %d}}
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
                 "fkIdPersonaEscribano": {"idPersona": %d}}
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

        mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idGestion").isNumber());
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
}
