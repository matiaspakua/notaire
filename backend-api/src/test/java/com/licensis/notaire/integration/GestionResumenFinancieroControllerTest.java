package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
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

@RequirementCoverage({"CU47", "CU02"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Gestion resumen financiero — CU47/CU02 aggregate financial summary endpoint")
class GestionResumenFinancieroControllerTest {

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

    private Integer createPersona(String numeroIdentificacion) throws Exception {
        String body = """
                {"nombre": "Escribano IT", "apellido": "Resumen Gestion IT", "numeroIdentificacion": "%s",
                 "esCliente": false, "tipoIdentificacion": {"idTipoIdentificacion": 1}}
                """.formatted(numeroIdentificacion);
        MvcResult result = mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPersona").asInt();
    }

    private Integer createPresupuesto(Integer clienteId, Float montoInmueble) throws Exception {
        String body = """
                {"numero": %d, "fecha": "2026-01-01", "encabezado": "Presupuesto Resumen Gestion IT",
                 "estado": "PENDIENTE", "monto": %s, "fkIdPersona": {"idPersona": %d}}
                """.formatted((int) (System.nanoTime() % 100000), montoInmueble, clienteId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPresupuesto").asInt();
    }

    private Integer createEstadoDeGestion() {
        EstadoDeGestion estado = new EstadoDeGestion();
        estado.setNombre("Estado Resumen Gestion IT");
        return estadoDeGestionRepository.save(estado).getIdEstadoGestion();
    }

    private Integer createTipoDeTramite() {
        TipoDeTramite tipo = new TipoDeTramite();
        tipo.setNombre("Tramite Resumen Gestion IT");
        tipo.setHabilitado(true);
        tipo.setSeArchiva(false);
        tipo.setSeInscribe(false);
        tipo.setAsociaInmuebles(false);
        return tipoDeTramiteRepository.save(tipo).getIdTipoTramite();
    }

    private Integer createGestionWithPresupuesto(Integer presupuestoId) throws Exception {
        Integer escribanoId = createPersona("64" + (System.nanoTime() % 1000000));
        Integer estadoId = createEstadoDeGestion();
        Integer tipoTramiteId = createTipoDeTramite();
        String body = """
                {"numero": %d, "encabezado": "Gestion Resumen IT", "presupuestoId": %d,
                 "escribanoId": %d, "estadoGestionId": %d, "tipoTramiteId": %d}
                """.formatted((int) (System.nanoTime() % 100000), presupuestoId, escribanoId, estadoId,
                tipoTramiteId);
        MvcResult result = mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idGestion").asInt();
    }

    private void createPago(Integer idPresupuesto, Float monto) throws Exception {
        String body = """
                {"idPresupuesto": %d, "monto": %s, "fecha": "2026-08-20", "observaciones": "Pago Resumen Gestion IT"}
                """.formatted(idPresupuesto, monto);
        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return the aggregate financial summary for a gestión with a payment")
    void shouldReturnResumenFinancieroForGestion() throws Exception {
        Integer clienteId = createPersona("65" + (System.nanoTime() % 1000000));
        Integer presupuestoId = createPresupuesto(clienteId, 5000.00f);
        createPago(presupuestoId, 2000.00f);
        Integer gestionId = createGestionWithPresupuesto(presupuestoId);

        mockMvc.perform(get("/api/v1/gestiones/" + gestionId + "/resumen-financiero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idGestion").value(gestionId))
                .andExpect(jsonPath("$.totalPresupuestado").value(5000.00))
                .andExpect(jsonPath("$.totalCobrado").value(2000.00))
                .andExpect(jsonPath("$.saldoPendiente").value(3000.00));
    }

    @Test
    @DisplayName("Should return 404 when requesting the financial summary of a non-existent gestión")
    void shouldReturnNotFoundForUnknownGestion() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/999999/resumen-financiero"))
                .andExpect(status().isNotFound());
    }
}
