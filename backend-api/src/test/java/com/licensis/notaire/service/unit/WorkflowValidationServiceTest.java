package com.licensis.notaire.service.unit;

import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowNodeType;
import com.licensis.notaire.negocio.WorkflowTransition;
import com.licensis.notaire.service.WorkflowValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("WorkflowValidationService Unit Tests")
class WorkflowValidationServiceTest {

    private WorkflowValidationService service;

    @BeforeEach
    void setUp() {
        service = new WorkflowValidationService();
    }

    @Test
    @DisplayName("Should validate a valid workflow")
    void shouldValidateValidWorkflow() {
        List<WorkflowNode> nodes = createValidNodes();
        List<WorkflowTransition> transitions = createValidTransitions(nodes);

        WorkflowValidationService.ValidationResult result = service.validate(nodes, transitions);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should reject workflow with no nodes")
    void shouldRejectWorkflowWithNoNodes() {
        List<WorkflowNode> nodes = new ArrayList<>();
        List<WorkflowTransition> transitions = new ArrayList<>();

        WorkflowValidationService.ValidationResult result = service.validate(nodes, transitions);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("El workflow no tiene nodos.");
    }

    @Test
    @DisplayName("Should reject workflow with no initial node")
    void shouldRejectWorkflowWithNoInitialNode() {
        List<WorkflowNode> nodes = new ArrayList<>();
        EstadoDeGestion estado1 = createEstado(1, "Estado1");
        nodes.add(createNode(1, WorkflowNodeType.FINAL, estado1));
        nodes.add(createNode(2, WorkflowNodeType.FINAL, null));

        List<WorkflowTransition> transitions = new ArrayList<>();

        WorkflowValidationService.ValidationResult result = service.validate(nodes, transitions);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("nodo inicial"));
    }

    @Test
    @DisplayName("Should reject workflow with multiple initial nodes")
    void shouldRejectWorkflowWithMultipleInitialNodes() {
        List<WorkflowNode> nodes = new ArrayList<>();
        nodes.add(createNode(1, WorkflowNodeType.INITIAL, null));
        nodes.add(createNode(2, WorkflowNodeType.INITIAL, null));
        nodes.add(createNode(3, WorkflowNodeType.FINAL, null));

        List<WorkflowTransition> transitions = new ArrayList<>();

        WorkflowValidationService.ValidationResult result = service.validate(nodes, transitions);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("más de un nodo inicial"));
    }

    @Test
    @DisplayName("Should reject workflow with no final node")
    void shouldRejectWorkflowWithNoFinalNode() {
        List<WorkflowNode> nodes = new ArrayList<>();
        nodes.add(createNode(1, WorkflowNodeType.INITIAL, null));
        nodes.add(createNode(2, WorkflowNodeType.INITIAL, createEstado(1, "Estado")));

        List<WorkflowTransition> transitions = new ArrayList<>();

        WorkflowValidationService.ValidationResult result = service.validate(nodes, transitions);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("nodo final"));
    }

    @Test
    @DisplayName("Should reject workflow with unreachable nodes")
    void shouldRejectWorkflowWithUnreachableNodes() {
        List<WorkflowNode> nodes = new ArrayList<>();
        EstadoDeGestion estado1 = createEstado(1, "Inicio");
        EstadoDeGestion estado2 = createEstado(2, "Fin");
        EstadoDeGestion estado3 = createEstado(3, "Aislado");

        nodes.add(createNode(1, WorkflowNodeType.INITIAL, estado1));
        nodes.add(createNode(2, WorkflowNodeType.FINAL, estado2));
        nodes.add(createNode(3, WorkflowNodeType.FINAL, estado3));

        List<WorkflowTransition> transitions = new ArrayList<>();
        transitions.add(createTransition(1, 2, nodes));

        WorkflowValidationService.ValidationResult result = service.validate(nodes, transitions);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("no alcanzables"));
    }

    @Test
    @DisplayName("Should validate workflow with linear path")
    void shouldValidateLinearWorkflow() {
        List<WorkflowNode> nodes = new ArrayList<>();
        nodes.add(createNode(1, WorkflowNodeType.INITIAL, null));
        nodes.add(createNode(2, WorkflowNodeType.FINAL, createEstado(1, "State1")));
        nodes.add(createNode(3, WorkflowNodeType.FINAL, null));

        List<WorkflowTransition> transitions = new ArrayList<>();
        transitions.add(createTransition(1, 2, nodes));
        transitions.add(createTransition(2, 3, nodes));

        WorkflowValidationService.ValidationResult result = service.validate(nodes, transitions);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should validate workflow with branching paths")
    void shouldValidateWorkflowWithBranching() {
        List<WorkflowNode> nodes = new ArrayList<>();
        nodes.add(createNode(1, WorkflowNodeType.INITIAL, null));
        nodes.add(createNode(2, WorkflowNodeType.FINAL, createEstado(1, "State1")));
        nodes.add(createNode(3, WorkflowNodeType.FINAL, createEstado(2, "State2")));
        nodes.add(createNode(4, WorkflowNodeType.FINAL, null));

        List<WorkflowTransition> transitions = new ArrayList<>();
        transitions.add(createTransition(1, 2, nodes));
        transitions.add(createTransition(1, 3, nodes));
        transitions.add(createTransition(2, 4, nodes));
        transitions.add(createTransition(3, 4, nodes));

        WorkflowValidationService.ValidationResult result = service.validate(nodes, transitions);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple initial failures")
    void shouldHandleMultipleFailures() {
        List<WorkflowNode> nodes = new ArrayList<>();
        nodes.add(createNode(1, WorkflowNodeType.INITIAL, null));
        nodes.add(createNode(2, WorkflowNodeType.INITIAL, null));

        List<WorkflowTransition> transitions = new ArrayList<>();

        WorkflowValidationService.ValidationResult result = service.validate(nodes, transitions);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2)
                .anyMatch(e -> e.contains("más de un nodo inicial"))
                .anyMatch(e -> e.contains("nodo final"));
    }

    @Test
    @DisplayName("Should validate result factory methods")
    void shouldValidateResultFactories() {
        WorkflowValidationService.ValidationResult validResult = WorkflowValidationService.ValidationResult.valid();
        assertThat(validResult.isValid()).isTrue();
        assertThat(validResult.getErrors()).isEmpty();

        List<String> errorList = List.of("Error1", "Error2");
        WorkflowValidationService.ValidationResult invalidResult =
                WorkflowValidationService.ValidationResult.invalid(errorList);
        assertThat(invalidResult.isValid()).isFalse();
        assertThat(invalidResult.getErrors()).isEqualTo(errorList);
    }

    private List<WorkflowNode> createValidNodes() {
        List<WorkflowNode> nodes = new ArrayList<>();
        nodes.add(createNode(1, WorkflowNodeType.INITIAL, null));
        nodes.add(createNode(2, WorkflowNodeType.FINAL, createEstado(1, "State")));
        nodes.add(createNode(3, WorkflowNodeType.FINAL, null));
        return nodes;
    }

    private List<WorkflowTransition> createValidTransitions(List<WorkflowNode> nodes) {
        List<WorkflowTransition> transitions = new ArrayList<>();
        transitions.add(createTransition(1, 2, nodes));
        transitions.add(createTransition(2, 3, nodes));
        return transitions;
    }

    private WorkflowNode createNode(Integer id, WorkflowNodeType tipo, EstadoDeGestion estado) {
        WorkflowNode node = new WorkflowNode();
        node.setId(id);
        node.setTipo(tipo);
        node.setEstadoDeGestion(estado);
        return node;
    }

    private WorkflowTransition createTransition(Integer fromId, Integer toId, List<WorkflowNode> nodes) {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setNodoOrigen(nodes.stream().filter(n -> n.getId().equals(fromId)).findFirst().orElse(null));
        transition.setNodoDestino(nodes.stream().filter(n -> n.getId().equals(toId)).findFirst().orElse(null));
        return transition;
    }

    private EstadoDeGestion createEstado(Integer id, String nombre) {
        EstadoDeGestion estado = new EstadoDeGestion();
        estado.setIdEstadoGestion(id);
        estado.setNombre(nombre);
        return estado;
    }
}
