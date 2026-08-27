package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for POST /api/v1/gestiones/{id}/transicionar.
 * Seeds a workflow (INITIAL -&gt; INTERMEDIATE, no transition to a disconnected
 * FINAL node) and asserts the endpoint applies valid transitions and rejects
 * invalid ones, per CU83.
 */
@RequirementCoverage({"CU83"})
@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
@DisplayName("POST /gestiones/{id}/transicionar — validates against the workflow definition (CU83)")
class GestionTransitionControllerIntegrationTest {

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
    private EstadoDeGestion estadoInicial;
    private EstadoDeGestion estadoIntermedio;
    private EstadoDeGestion estadoInalcanzable;
    private Integer gestionId;
    private Integer gestionSinTramiteId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        seedWorkflowAndGestion();
    }

    private EstadoDeGestion estado(String nombre) {
        EstadoDeGestion estado = new EstadoDeGestion();
        estado.setNombre(nombre + " " + System.nanoTime());
        return estadoRepository.save(estado);
    }

    private void seedWorkflowAndGestion() {
        estadoInicial = estado("Transicion Inicial");
        estadoIntermedio = estado("Transicion Intermedio");
        estadoInalcanzable = estado("Transicion Inalcanzable");

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setNombre("Workflow Transicion IT " + System.nanoTime());
        definition.setActivo(true);
        definition = workflowDefinitionRepository.save(definition);

        WorkflowNode nodoInicial = new WorkflowNode();
        nodoInicial.setWorkflowDefinition(definition);
        nodoInicial.setEstadoDeGestion(estadoInicial);
        nodoInicial.setTipo(WorkflowNodeType.INITIAL);
        nodoInicial = workflowNodeRepository.save(nodoInicial);

        WorkflowNode nodoIntermedio = new WorkflowNode();
        nodoIntermedio.setWorkflowDefinition(definition);
        nodoIntermedio.setEstadoDeGestion(estadoIntermedio);
        nodoIntermedio.setTipo(WorkflowNodeType.FINAL);
        workflowNodeRepository.save(nodoIntermedio);

        WorkflowTransition transicion = new WorkflowTransition();
        transicion.setWorkflowDefinition(definition);
        transicion.setNodoOrigen(nodoInicial);
        transicion.setNodoDestino(nodoIntermedio);
        workflowTransitionRepository.save(transicion);

        TipoDeTramite tipo = new TipoDeTramite();
        tipo.setNombre("Tipo Transicion IT");
        tipo.setWorkflowDefinition(definition);
        tipo = tipoDeTramiteRepository.save(tipo);

        gestionId = createGestion(estadoInicial, tipo, true);
        gestionSinTramiteId = createGestion(estadoInicial, tipo, false);

        entityManager.flush();
        entityManager.clear();
    }

    private Integer createGestion(EstadoDeGestion estado, TipoDeTramite tipo, boolean conTramite) {
        GestionDeEscritura gestion = new GestionDeEscritura();
        gestion.setNumero((int) (System.nanoTime() % 100000));
        gestion.setEncabezado("Gestion Transicion IT");
        gestion.setFechaInicio(new Date());
        gestion.setFkIdEstadoDeGestion(estado);
        gestion.setFkIdPersonaEscribano(personaRepository.findAll().get(0));
        gestion = gestionRepository.save(gestion);

        if (conTramite) {
            Tramite tramite = new Tramite();
            tramite.setFkIdGestion(gestion);
            tramite.setFkIdTipoTramite(tipo);
            tramiteRepository.save(tramite);
        }
        return gestion.getIdGestion();
    }

    @Test
    @DisplayName("Transición válida se aplica")
    void shouldApplyValidTransition() throws Exception {
        mockMvc.perform(post("/api/v1/gestiones/{id}/transicionar", gestionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoDestino\": \"" + estadoIntermedio.getNombre() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoActual").value(estadoIntermedio.getNombre()));

        assertThat(gestionRepository.findById(gestionId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdEstadoDeGestion().getNombre())
                .isEqualTo(estadoIntermedio.getNombre());
        assertThat(historialRepository.findByFkIdGestionIdGestion(gestionId)).isNotEmpty();
    }

    @Test
    @DisplayName("Transición inválida es rechazada")
    void shouldRejectInvalidTransition() throws Exception {
        mockMvc.perform(post("/api/v1/gestiones/{id}/transicionar", gestionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoDestino\": \"" + estadoInalcanzable.getNombre() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        assertThat(gestionRepository.findById(gestionId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdEstadoDeGestion().getNombre())
                .isEqualTo(estadoInicial.getNombre());
    }

    @Test
    @DisplayName("Gestión sin workflow definido rechaza cualquier transición")
    void shouldRejectTransitionWhenNoWorkflowDefinition() throws Exception {
        mockMvc.perform(post("/api/v1/gestiones/{id}/transicionar", gestionSinTramiteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoDestino\": \"" + estadoIntermedio.getNombre() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 404 when transitioning a gestión that does not exist")
    void shouldReturn404WhenGestionDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/v1/gestiones/999999/transicionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoDestino\": \"" + estadoIntermedio.getNombre() + "\"}"))
                .andExpect(status().isNotFound());
    }
}
