package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowNodeType;
import com.licensis.notaire.negocio.WorkflowTransition;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU16", "RF-22", "RF-37"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Gestion archive — verifies pending debt before and while archiving (CU16, RF-22, RF-37)")
class GestionArchiveIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EstadoDeGestionRepository estadoDeGestionRepository;

    @Autowired
    private TipoDeTramiteRepository tipoDeTramiteRepository;

    @Autowired
    private GestionDeEscrituraRepository gestionDeEscrituraRepository;

    @Autowired
    private WorkflowDefinitionRepository workflowDefinitionRepository;

    @Autowired
    private WorkflowNodeRepository workflowNodeRepository;

    @Autowired
    private WorkflowTransitionRepository workflowTransitionRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private EstadoDeGestion archivada;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        archivada = estadoDeGestionRepository.findByNombre("Archivada").orElseGet(() -> {
            EstadoDeGestion nuevo = new EstadoDeGestion();
            nuevo.setNombre("Archivada");
            return estadoDeGestionRepository.save(nuevo);
        });
    }

    private Integer createPersona(String numeroIdentificacion) throws Exception {
        String body = """
                {"nombre": "Escribano IT", "apellido": "Archive IT", "numeroIdentificacion": "%s",
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
                {"numero": %d, "fecha": "2026-01-01", "encabezado": "Presupuesto Archive IT",
                 "estado": "PENDIENTE", "monto": %s, "fkIdPersona": {"idPersona": %d}}
                """.formatted((int) (System.nanoTime() % 100000), montoInmueble, clienteId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPresupuesto").asInt();
    }

    private EstadoDeGestion createEstadoDeGestion() {
        EstadoDeGestion estado = new EstadoDeGestion();
        estado.setNombre("Estado Archive IT " + System.nanoTime());
        return estadoDeGestionRepository.save(estado);
    }

    private Integer createTipoDeTramiteConWorkflowHaciaArchivada(EstadoDeGestion estadoInicial) {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setNombre("Workflow Archive IT " + System.nanoTime());
        definition.setActivo(true);
        definition = workflowDefinitionRepository.save(definition);

        WorkflowNode nodoOrigen = new WorkflowNode();
        nodoOrigen.setWorkflowDefinition(definition);
        nodoOrigen.setEstadoDeGestion(estadoInicial);
        nodoOrigen.setTipo(WorkflowNodeType.INITIAL);
        nodoOrigen = workflowNodeRepository.save(nodoOrigen);

        WorkflowNode nodoDestino = new WorkflowNode();
        nodoDestino.setWorkflowDefinition(definition);
        nodoDestino.setEstadoDeGestion(archivada);
        nodoDestino.setTipo(WorkflowNodeType.FINAL);
        nodoDestino = workflowNodeRepository.save(nodoDestino);

        WorkflowTransition transicion = new WorkflowTransition();
        transicion.setWorkflowDefinition(definition);
        transicion.setNodoOrigen(nodoOrigen);
        transicion.setNodoDestino(nodoDestino);
        workflowTransitionRepository.save(transicion);

        TipoDeTramite tipo = new TipoDeTramite();
        tipo.setNombre("Tramite Archive IT");
        tipo.setHabilitado(true);
        tipo.setSeArchiva(false);
        tipo.setSeInscribe(false);
        tipo.setAsociaInmuebles(false);
        tipo.setWorkflowDefinition(definition);
        return tipoDeTramiteRepository.save(tipo).getIdTipoTramite();
    }

    private Integer createGestionWithPresupuesto(Float montoInmueble) throws Exception {
        Integer clienteId = createPersona("43" + (System.nanoTime() % 1000000));
        Integer escribanoId = createPersona("44" + (System.nanoTime() % 1000000));
        Integer presupuestoId = createPresupuesto(clienteId, montoInmueble);
        EstadoDeGestion estadoInicial = createEstadoDeGestion();
        Integer estadoId = estadoInicial.getIdEstadoGestion();
        Integer tipoTramiteId = createTipoDeTramiteConWorkflowHaciaArchivada(estadoInicial);
        String body = """
                {"numero": %d, "encabezado": "Gestion Archive IT", "presupuestoId": %d,
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

    @Test
    @DisplayName("Archiving a gestión with pending debt reflects the aggregate balance in the response")
    void shouldReportPendingDebtWhenArchivingGestionWithBalance() throws Exception {
        Integer gestionId = createGestionWithPresupuesto(5000.00f);

        mockMvc.perform(get("/api/v1/gestiones/" + gestionId + "/saldo-pendiente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoPendiente").value(5000.00));

        mockMvc.perform(post("/api/v1/gestiones/" + gestionId + "/archivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idGestion").value(gestionId))
                .andExpect(jsonPath("$.saldoPendiente").value(5000.00))
                .andExpect(jsonPath("$.deudaPendienteAlArchivar").value(true));
    }

    @Test
    @DisplayName("Confirming archiving despite pending debt still archives the gestión")
    void shouldArchiveGestionEvenWhenPendingDebtExists() throws Exception {
        Integer gestionId = createGestionWithPresupuesto(3000.00f);

        mockMvc.perform(post("/api/v1/gestiones/" + gestionId + "/archivar"))
                .andExpect(status().isOk());

        assertThat(gestionDeEscrituraRepository.findById(gestionId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdEstadoDeGestion().getNombre())
                .isEqualTo("Archivada");
    }

    @Test
    @DisplayName("Archiving a gestión with no pending debt reports zero balance")
    void shouldArchiveGestionWithoutDebtWarning() throws Exception {
        Integer gestionId = createGestionWithPresupuesto(0.00f);

        mockMvc.perform(post("/api/v1/gestiones/" + gestionId + "/archivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoPendiente").value(0.00))
                .andExpect(jsonPath("$.deudaPendienteAlArchivar").value(false));
    }

    @Test
    @DisplayName("Archiving record reflects pending debt when the gestión had an outstanding balance")
    void shouldPersistDeudaPendienteTrueWhenBalanceIsPositive() throws Exception {
        Integer gestionId = createGestionWithPresupuesto(1234.00f);

        mockMvc.perform(post("/api/v1/gestiones/" + gestionId + "/archivar"))
                .andExpect(status().isOk());

        assertThat(gestionDeEscrituraRepository.findById(gestionId))
                .isPresent()
                .get()
                .extracting("deudaPendienteAlArchivar")
                .isEqualTo(true);
    }

    @Test
    @DisplayName("Archiving record reflects no pending debt when the gestión had a zero balance")
    void shouldPersistDeudaPendienteFalseWhenBalanceIsZero() throws Exception {
        Integer gestionId = createGestionWithPresupuesto(0.00f);

        mockMvc.perform(post("/api/v1/gestiones/" + gestionId + "/archivar"))
                .andExpect(status().isOk());

        assertThat(gestionDeEscrituraRepository.findById(gestionId))
                .isPresent()
                .get()
                .extracting("deudaPendienteAlArchivar")
                .isEqualTo(false);
    }

    @Test
    @DisplayName("Should return 404 when archiving a gestión that does not exist")
    void shouldReturn404WhenArchivingMissingGestion() throws Exception {
        mockMvc.perform(post("/api/v1/gestiones/999999/archivar"))
                .andExpect(status().isNotFound());
    }
}
