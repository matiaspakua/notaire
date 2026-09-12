package com.licensis.notaire.integration;

import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowNodeType;
import com.licensis.notaire.business.WorkflowTransition;
import com.licensis.notaire.repository.ManagementStatusRepository;
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
    private ManagementStatusRepository managementStatusRepository;

    private WorkflowDefinition workflowDefinition;
    private WorkflowNode nodeStart;
    private WorkflowNode nodeIntermedio;
    private WorkflowNode nodeFinal;

    @BeforeEach
    void setUp() {
        workflowTransitionRepository.deleteAll();
        workflowNodeRepository.deleteAll();
        workflowDefinitionRepository.deleteAll();
        managementStatusRepository.deleteAll();

        // Create workflow definition
        workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setName("Workflow Test");
        workflowDefinition.setDescription("Workflow para pruebas");
        workflowDefinition = workflowDefinitionRepository.save(workflowDefinition);

        // Create states
        ManagementStatus statusStart = new ManagementStatus();
        statusStart.setName("Inicio");
        statusStart = managementStatusRepository.save(statusStart);

        ManagementStatus statusProceso = new ManagementStatus();
        statusProceso.setName("En Proceso");
        statusProceso = managementStatusRepository.save(statusProceso);

        ManagementStatus statusFinal = new ManagementStatus();
        statusFinal.setName("Finalizado");
        statusFinal = managementStatusRepository.save(statusFinal);

        // Create nodes
        nodeStart = new WorkflowNode();
        nodeStart.setWorkflowDefinition(workflowDefinition);
        nodeStart.setManagementStatus(statusStart);
        nodeStart.setType(WorkflowNodeType.INITIAL);
        nodeStart = workflowNodeRepository.save(nodeStart);

        nodeIntermedio = new WorkflowNode();
        nodeIntermedio.setWorkflowDefinition(workflowDefinition);
        nodeIntermedio.setManagementStatus(statusProceso);
        nodeIntermedio.setType(WorkflowNodeType.INTERMEDIATE);
        nodeIntermedio = workflowNodeRepository.save(nodeIntermedio);

        nodeFinal = new WorkflowNode();
        nodeFinal.setWorkflowDefinition(workflowDefinition);
        nodeFinal.setManagementStatus(statusFinal);
        nodeFinal.setType(WorkflowNodeType.FINAL);
        nodeFinal = workflowNodeRepository.save(nodeFinal);
    }

    @Test
    @DisplayName("Should create valid workflow transition")
    void shouldCreateValidWorkflowTransition() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setOriginNode(nodeStart);
        transition.setDestinationNode(nodeIntermedio);
        transition.setDescription("Transición de inicio a proceso");
        transition.setCondition("approved");

        WorkflowTransition saved = workflowTransitionRepository.save(transition);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getWorkflowDefinition().getId()).isEqualTo(workflowDefinition.getId());
        assertThat(saved.getOriginNode().getId()).isEqualTo(nodeStart.getId());
        assertThat(saved.getDestinationNode().getId()).isEqualTo(nodeIntermedio.getId());
    }

    @Test
    @DisplayName("Should retrieve transition by ID")
    void shouldRetrieveTransitionById() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setOriginNode(nodeStart);
        transition.setDestinationNode(nodeIntermedio);
        WorkflowTransition saved = workflowTransitionRepository.save(transition);

        Optional<WorkflowTransition> found = workflowTransitionRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getOriginNode().getId()).isEqualTo(nodeStart.getId());
    }

    @Test
    @DisplayName("Should update transition description")
    void shouldUpdateTransitionDescription() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setOriginNode(nodeStart);
        transition.setDestinationNode(nodeIntermedio);
        transition.setDescription("Original");
        WorkflowTransition saved = workflowTransitionRepository.save(transition);

        saved.setDescription("Actualizado");
        WorkflowTransition updated = workflowTransitionRepository.save(saved);

        assertThat(updated.getDescription()).isEqualTo("Actualizado");
    }

    @Test
    @DisplayName("Should delete transition")
    void shouldDeleteTransition() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setOriginNode(nodeStart);
        transition.setDestinationNode(nodeIntermedio);
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
            transition.setOriginNode(nodeStart);
            transition.setDestinationNode(i == 0 ? nodeIntermedio : nodeFinal);
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
            transition.setOriginNode(nodeStart);
            transition.setDestinationNode(nodeIntermedio);
            transition.setCondition(condition);
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
        transition.setOriginNode(nodeStart);
        transition.setDestinationNode(nodeIntermedio);
        transition.setCondition(null);

        WorkflowTransition saved = workflowTransitionRepository.save(transition);

        assertThat(saved.getCondition()).isNull();
    }

    @Test
    @DisplayName("Should validate required relationships")
    void shouldValidateRequiredRelationships() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setOriginNode(nodeStart);
        transition.setDestinationNode(nodeIntermedio);

        WorkflowTransition saved = workflowTransitionRepository.save(transition);

        assertThat(saved.getWorkflowDefinition()).isNotNull();
        assertThat(saved.getOriginNode()).isNotNull();
        assertThat(saved.getDestinationNode()).isNotNull();
    }

    @Test
    @DisplayName("Should maintain node relationship integrity")
    void shouldMaintainNodeRelationshipIntegrity() {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(workflowDefinition);
        transition.setOriginNode(nodeStart);
        transition.setDestinationNode(nodeFinal);
        WorkflowTransition saved = workflowTransitionRepository.save(transition);

        WorkflowTransition retrieved = workflowTransitionRepository.findById(saved.getId()).orElseThrow();

        assertThat(retrieved.getOriginNode().getType()).isEqualTo(WorkflowNodeType.INITIAL);
        assertThat(retrieved.getDestinationNode().getType()).isEqualTo(WorkflowNodeType.FINAL);
        assertThat(retrieved.getOriginNode().getId()).isNotEqualTo(retrieved.getDestinationNode().getId());
    }

    @Test
    @DisplayName("Should support complex workflow scenarios")
    void shouldSupportComplexWorkflowScenarios() {
        // Create linear flow
        WorkflowTransition t1 = new WorkflowTransition();
        t1.setWorkflowDefinition(workflowDefinition);
        t1.setOriginNode(nodeStart);
        t1.setDestinationNode(nodeIntermedio);
        t1.setCondition("auto_approved");
        t1.setDescription("Automatic approval path");
        workflowTransitionRepository.save(t1);

        WorkflowTransition t2 = new WorkflowTransition();
        t2.setWorkflowDefinition(workflowDefinition);
        t2.setOriginNode(nodeIntermedio);
        t2.setDestinationNode(nodeFinal);
        t2.setCondition("manual_approved");
        t2.setDescription("Manual approval path");
        workflowTransitionRepository.save(t2);

        // Create alternative path
        WorkflowTransition t3 = new WorkflowTransition();
        t3.setWorkflowDefinition(workflowDefinition);
        t3.setOriginNode(nodeStart);
        t3.setDestinationNode(nodeFinal);
        t3.setCondition("admin_override");
        t3.setDescription("Admin fast-track");
        workflowTransitionRepository.save(t3);

        List<WorkflowTransition> all = workflowTransitionRepository.findAll();
        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("Should handle batch transition creation")
    void shouldHandleBatchTransitionCreation() {
        for (int i = 0; i < 10; i++) {
            WorkflowNode origin = workflowNodeRepository.findById(nodeStart.getId()).orElseThrow();
            WorkflowNode destination = workflowNodeRepository.findById(
                    i % 2 == 0 ? nodeIntermedio.getId() : nodeFinal.getId()
            ).orElseThrow();

            WorkflowTransition transition = new WorkflowTransition();
            transition.setWorkflowDefinition(workflowDefinition);
            transition.setOriginNode(origin);
            transition.setDestinationNode(destination);
            transition.setCondition("batch_" + i);
            workflowTransitionRepository.save(transition);
        }

        List<WorkflowTransition> all = workflowTransitionRepository.findAll();
        assertThat(all).hasSize(10);
    }
}
