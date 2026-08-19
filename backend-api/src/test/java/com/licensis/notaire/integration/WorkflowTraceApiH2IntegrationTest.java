package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowNodeType;
import com.licensis.notaire.negocio.WorkflowTransition;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.HistorialRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for GET /api/v1/gestiones/{id}/workflow-trace.
 * Seeds a workflow (INITIAL → INTERMEDIATE → FINAL), a gestión whose trámite
 * uses that workflow's tipo, and historial entries covering the first two
 * estados, then asserts the aggregated trace and computed node statuses.
 */
@RequirementCoverage({"CU83"})
@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
@DisplayName("CU83 — Workflow trace endpoint integration tests (H2)")
class WorkflowTraceApiH2IntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private EstadoDeGestionRepository estadoRepository;
    @Autowired
    private WorkflowDefinitionRepository workflowDefinitionRepository;
    @Autowired
    private WorkflowNodeRepository workflowNodeRepository;
    @Autowired
    private WorkflowTransitionRepository workflowTransitionRepository;
    @Autowired
    private TipoDeTramiteRepository tipoDeTramiteRepository;
    @Autowired
    private GestionDeEscrituraRepository gestionRepository;
    @Autowired
    private TramiteRepository tramiteRepository;
    @Autowired
    private HistorialRepository historialRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private EntityManager entityManager;

    private MockMvc mockMvc;
    private Integer gestionId;
    private Integer initialNodeId;
    private Integer middleNodeId;
    private Integer finalNodeId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        seedWorkflowAndGestion();
    }

    private EstadoDeGestion estado(String nombre) {
        EstadoDeGestion estado = new EstadoDeGestion();
        estado.setNombre(nombre);
        return estadoRepository.save(estado);
    }

    private WorkflowNode node(WorkflowDefinition def, EstadoDeGestion estado, WorkflowNodeType tipo) {
        WorkflowNode node = new WorkflowNode();
        node.setWorkflowDefinition(def);
        node.setEstadoDeGestion(estado);
        node.setTipo(tipo);
        return workflowNodeRepository.save(node);
    }

    private Historial historial(GestionDeEscritura gestion, EstadoDeGestion estado, long epochMillis) {
        Historial entry = new Historial();
        entry.setFkIdGestion(gestion);
        entry.setFkIdEstadoGestion(estado);
        entry.setFecha(new Date(epochMillis));
        return historialRepository.save(entry);
    }

    private void seedWorkflowAndGestion() {
        EstadoDeGestion iniciada = estado("Trace Iniciada");
        EstadoDeGestion enCurso = estado("Trace En Curso");
        EstadoDeGestion cerrada = estado("Trace Cerrada");

        WorkflowDefinition def = new WorkflowDefinition();
        def.setNombre("Workflow Trace Test");
        def.setActivo(true);
        def = workflowDefinitionRepository.save(def);

        WorkflowNode initial = node(def, iniciada, WorkflowNodeType.INITIAL);
        WorkflowNode middle = node(def, enCurso, WorkflowNodeType.INTERMEDIATE);
        WorkflowNode last = node(def, cerrada, WorkflowNodeType.FINAL);
        initialNodeId = initial.getId();
        middleNodeId = middle.getId();
        finalNodeId = last.getId();

        WorkflowTransition t1 = new WorkflowTransition();
        t1.setWorkflowDefinition(def);
        t1.setNodoOrigen(initial);
        t1.setNodoDestino(middle);
        workflowTransitionRepository.save(t1);
        WorkflowTransition t2 = new WorkflowTransition();
        t2.setWorkflowDefinition(def);
        t2.setNodoOrigen(middle);
        t2.setNodoDestino(last);
        workflowTransitionRepository.save(t2);

        TipoDeTramite tipo = new TipoDeTramite();
        tipo.setNombre("Tipo Trace Test");
        tipo.setWorkflowDefinition(def);
        tipo = tipoDeTramiteRepository.save(tipo);

        GestionDeEscritura gestion = new GestionDeEscritura();
        gestion.setIdGestion(null);
        gestion.setNumero(987654);
        gestion.setEncabezado("Gestion Trace Test");
        gestion.setFechaInicio(new Date());
        gestion.setFkIdEstadoDeGestion(enCurso);
        gestion.setFkIdPersonaEscribano(personaRepository.findAll().get(0));
        gestion = gestionRepository.save(gestion);
        gestionId = gestion.getIdGestion();

        Tramite tramite = new Tramite();
        tramite.setIdTramite(null);
        tramite.setFkIdGestion(gestion);
        tramite.setFkIdTipoTramite(tipo);
        tramiteRepository.save(tramite);

        long oneDayMillis = 86_400_000L;
        historial(gestion, iniciada, oneDayMillis);
        historial(gestion, enCurso, 2 * oneDayMillis);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should return aggregated trace with nodes, transitions and historial")
    void shouldReturnAggregatedTraceWithNodesTransitionsAndHistorial() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/{id}/workflow-trace", gestionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gestionId").value(gestionId))
                .andExpect(jsonPath("$.numero").value(987654))
                .andExpect(jsonPath("$.encabezado").value("Gestion Trace Test"))
                .andExpect(jsonPath("$.estadoActual").value("Trace En Curso"))
                .andExpect(jsonPath("$.workflowDefinition.nombre").value("Workflow Trace Test"))
                .andExpect(jsonPath("$.nodes", hasSize(3)))
                .andExpect(jsonPath("$.transitions", hasSize(2)))
                .andExpect(jsonPath("$.historial", hasSize(2)));
    }

    @Test
    @DisplayName("Should compute completed, in_progress and pending node statuses")
    void shouldComputeNodeStatusesFromHistorial() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/{id}/workflow-trace", gestionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeStatuses." + initialNodeId).value("completed"))
                .andExpect(jsonPath("$.nodeStatuses." + middleNodeId).value("in_progress"))
                .andExpect(jsonPath("$.nodeStatuses." + finalNodeId).value("pending"));
    }

    @Test
    @DisplayName("Should return 400 when gestion does not exist")
    void shouldReturnBadRequestWhenGestionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/{id}/workflow-trace", 999999))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should serialize gestiones list when tramite tipo has a workflow definition")
    void shouldSerializeGestionesListWhenTipoHasWorkflowDefinition() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idGestion").value(gestionId))
                .andExpect(jsonPath("$.content[0].tramiteCount").value(1));
    }

    @Test
    @DisplayName("Should serialize gestion by numero when tramite tipo has a workflow definition")
    void shouldSerializeGestionByNumeroWhenTipoHasWorkflowDefinition() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/numero/{numero}", 987654))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(987654))
                .andExpect(jsonPath("$.estadoActual").value("Trace En Curso"))
                .andExpect(jsonPath("$.tramiteCount").value(1));
    }

    @Test
    @DisplayName("Should serialize tipo-tramite list when a workflow definition is assigned")
    void shouldSerializeTipoTramiteListWhenWorkflowDefinitionAssigned() throws Exception {
        mockMvc.perform(get("/api/v1/tipo-tramite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == 'Tipo Trace Test')].workflowDefinitionNombre")
                        .value("Workflow Trace Test"));
    }

    @Test
    @DisplayName("Should serialize historial endpoints without entity cycles")
    void shouldSerializeHistorialEndpointsWithoutEntityCycles() throws Exception {
        mockMvc.perform(get("/api/v1/historial"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/historial/gestion/{id}", gestionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].gestionId").value(gestionId));
        mockMvc.perform(get("/api/v1/gestiones/{id}/estado-actual", gestionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoGestionNombre").value("Trace En Curso"));
    }
}
