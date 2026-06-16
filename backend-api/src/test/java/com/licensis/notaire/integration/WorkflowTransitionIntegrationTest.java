package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowNodeType;
import com.licensis.notaire.negocio.WorkflowTransition;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowTransition Integration Tests")
class WorkflowTransitionIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WorkflowTransitionRepository workflowTransitionRepository;

    @Autowired
    private WorkflowDefinitionRepository workflowDefinitionRepository;

    @Autowired
    private WorkflowNodeRepository workflowNodeRepository;

    @Autowired
    private EstadoDeGestionRepository estadoDeGestionRepository;

    private WorkflowDefinition workflowDefinition;
    private WorkflowNode nodoInicio;
    private WorkflowNode nodoIntermedio;
    private WorkflowNode nodoFinal;

    @BeforeEach
    void setUp() {
        workflowTransitionRepository.deleteAll();
        workflowNodeRepository.deleteAll();
        workflowDefinitionRepository.deleteAll();
        estadoDeGestionRepository.deleteAll();

        // Create workflow definition
        workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setNombre("Workflow Test");
        workflowDefinition.setDescripcion("Workflow para pruebas");
        workflowDefinition = workflowDefinitionRepository.save(workflowDefinition);

        // Create states
        EstadoDeGestion estadoInicio = new EstadoDeGestion();
        estadoInicio.setNombre("Inicio");
        estadoInicio = estadoDeGestionRepository.save(estadoInicio);

        EstadoDeGestion estadoProceso = new EstadoDeGestion();
        estadoProceso.setNombre("En Proceso");
        estadoProceso = estadoDeGestionRepository.save(estadoProceso);

        EstadoDeGestion estadoFinal = new EstadoDeGestion();
        estadoFinal.setNombre("Finalizado");
        estadoFinal = estadoDeGestionRepository.save(estadoFinal);

        // Create nodes
        nodoInicio = new WorkflowNode();
        nodoInicio.setWorkflowDefinition(workflowDefinition);
        nodoInicio.setEstadoDeGestion(estadoInicio);
        nodoInicio.setTipo(WorkflowNodeType.INITIAL);
        nodoInicio = workflowNodeRepository.save(nodoInicio);

        nodoIntermedio = new WorkflowNode();
        nodoIntermedio.setWorkflowDefinition(workflowDefinition);
        nodoIntermedio.setEstadoDeGestion(estadoProceso);
        nodoIntermedio.setTipo(WorkflowNodeType.INTERMEDIATE);
        nodoIntermedio = workflowNodeRepository.save(nodoIntermedio);

        nodoFinal = new WorkflowNode();
        nodoFinal.setWorkflowDefinition(workflowDefinition);
        nodoFinal.setEstadoDeGestion(estadoFinal);
        nodoFinal.setTipo(WorkflowNodeType.FINAL);
        nodoFinal = workflowNodeRepository.save(nodoFinal);
    }

    @Test
    @DisplayName("Should create valid workflow transition")
    void shouldCreateValidWorkflowTransition() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setNodoOrigen(nodoInicio);
        transition.setNodoDestino(nodoIntermedio);
        transition.setDescripcion("Transición de inicio a proceso");
        transition.setCondicion("approved");

        WorkflowTransition saved = workflowTransitionRepository.save(transition);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getWorkflowDefinition().getId()).isEqualTo(workflowDefinition.getId());
        assertThat(saved.getNodoOrigen().getId()).isEqualTo(nodoInicio.getId());
        assertThat(saved.getNodoDestino().getId()).isEqualTo(nodoIntermedio.getId());
    }

    @Test
    @DisplayName("Should retrieve transition by ID")
    void shouldRetrieveTransitionById() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setNodoOrigen(nodoInicio);
        transition.setNodoDestino(nodoIntermedio);
        WorkflowTransition saved = workflowTransitionRepository.save(transition);

        Optional<WorkflowTransition> found = workflowTransitionRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getNodoOrigen().getId()).isEqualTo(nodoInicio.getId());
    }

    @Test
    @DisplayName("Should update transition description")
    void shouldUpdateTransitionDescription() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setNodoOrigen(nodoInicio);
        transition.setNodoDestino(nodoIntermedio);
        transition.setDescripcion("Original");
        WorkflowTransition saved = workflowTransitionRepository.save(transition);

        saved.setDescripcion("Actualizado");
        WorkflowTransition updated = workflowTransitionRepository.save(saved);

        assertThat(updated.getDescripcion()).isEqualTo("Actualizado");
    }

    @Test
    @DisplayName("Should delete transition")
    void shouldDeleteTransition() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setNodoOrigen(nodoInicio);
        transition.setNodoDestino(nodoIntermedio);
        WorkflowTransition saved = workflowTransitionRepository.save(transition);
        Integer id = saved.getId();

        workflowTransitionRepository.delete(saved);

        Optional<WorkflowTransition> deleted = workflowTransitionRepository.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should support multiple transitions from same origin node")
    void shouldSupportMultipleTransitionsFromSameOriginNode() {
        for (int i = 0; i < 3; i++) {
            WorkflowTransition transition = new WorkflowTransition();
            transition.setWorkflowDefinition(workflowDefinition);
            transition.setNodoOrigen(nodoInicio);
            transition.setNodoDestino(i == 0 ? nodoIntermedio : nodoFinal);
            workflowTransitionRepository.save(transition);
        }

        List<WorkflowTransition> all = workflowTransitionRepository.findAll();
        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("Should handle transition conditions")
    void shouldHandleTransitionConditions() {
        String[] conditions = {"approved", "rejected", "pending_review", "escalated", "timeout"};

        for (String condition : conditions) {
            WorkflowTransition transition = new WorkflowTransition();
            transition.setWorkflowDefinition(workflowDefinition);
            transition.setNodoOrigen(nodoInicio);
            transition.setNodoDestino(nodoIntermedio);
            transition.setCondicion(condition);
            workflowTransitionRepository.save(transition);
        }

        List<WorkflowTransition> all = workflowTransitionRepository.findAll();
        assertThat(all).hasSize(5);
    }

    @Test
    @DisplayName("Should support transitions with null condition")
    void shouldSupportTransitionsWithNullCondition() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setNodoOrigen(nodoInicio);
        transition.setNodoDestino(nodoIntermedio);
        transition.setCondicion(null);

        WorkflowTransition saved = workflowTransitionRepository.save(transition);

        assertThat(saved.getCondicion()).isNull();
    }

    @Test
    @DisplayName("Should validate required relationships")
    void shouldValidateRequiredRelationships() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setNodoOrigen(nodoInicio);
        transition.setNodoDestino(nodoIntermedio);

        WorkflowTransition saved = workflowTransitionRepository.save(transition);

        assertThat(saved.getWorkflowDefinition()).isNotNull();
        assertThat(saved.getNodoOrigen()).isNotNull();
        assertThat(saved.getNodoDestino()).isNotNull();
    }

    @Test
    @DisplayName("Should maintain node relationship integrity")
    void shouldMaintainNodeRelationshipIntegrity() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setNodoOrigen(nodoInicio);
        transition.setNodoDestino(nodoFinal);
        WorkflowTransition saved = workflowTransitionRepository.save(transition);

        WorkflowTransition retrieved = workflowTransitionRepository.findById(saved.getId()).orElseThrow();

        assertThat(retrieved.getNodoOrigen().getTipo()).isEqualTo(WorkflowNodeType.INITIAL);
        assertThat(retrieved.getNodoDestino().getTipo()).isEqualTo(WorkflowNodeType.FINAL);
        assertThat(retrieved.getNodoOrigen().getId()).isNotEqualTo(retrieved.getNodoDestino().getId());
    }

    @Test
    @DisplayName("Should support complex workflow scenarios")
    void shouldSupportComplexWorkflowScenarios() {
        // Create linear flow
        WorkflowTransition t1 = new WorkflowTransition();
        t1.setWorkflowDefinition(workflowDefinition);
        t1.setNodoOrigen(nodoInicio);
        t1.setNodoDestino(nodoIntermedio);
        t1.setCondicion("auto_approved");
        t1.setDescripcion("Automatic approval path");
        workflowTransitionRepository.save(t1);

        WorkflowTransition t2 = new WorkflowTransition();
        t2.setWorkflowDefinition(workflowDefinition);
        t2.setNodoOrigen(nodoIntermedio);
        t2.setNodoDestino(nodoFinal);
        t2.setCondicion("manual_approved");
        t2.setDescripcion("Manual approval path");
        workflowTransitionRepository.save(t2);

        // Create alternative path
        WorkflowTransition t3 = new WorkflowTransition();
        t3.setWorkflowDefinition(workflowDefinition);
        t3.setNodoOrigen(nodoInicio);
        t3.setNodoDestino(nodoFinal);
        t3.setCondicion("admin_override");
        t3.setDescripcion("Admin fast-track");
        workflowTransitionRepository.save(t3);

        List<WorkflowTransition> all = workflowTransitionRepository.findAll();
        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("Should handle batch transition creation")
    void shouldHandleBatchTransitionCreation() {
        for (int i = 0; i < 10; i++) {
            WorkflowNode origen = workflowNodeRepository.findById(nodoInicio.getId()).orElseThrow();
            WorkflowNode destino = workflowNodeRepository.findById(
                    i % 2 == 0 ? nodoIntermedio.getId() : nodoFinal.getId()
            ).orElseThrow();

            WorkflowTransition transition = new WorkflowTransition();
            transition.setWorkflowDefinition(workflowDefinition);
            transition.setNodoOrigen(origen);
            transition.setNodoDestino(destino);
            transition.setCondicion("batch_" + i);
            workflowTransitionRepository.save(transition);
        }

        List<WorkflowTransition> all = workflowTransitionRepository.findAll();
        assertThat(all).hasSize(10);
    }
}
