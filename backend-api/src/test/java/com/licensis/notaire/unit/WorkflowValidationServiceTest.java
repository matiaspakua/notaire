package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowNodeType;
import com.licensis.notaire.negocio.WorkflowTransition;
import com.licensis.notaire.service.WorkflowValidationService;
import com.licensis.notaire.service.WorkflowValidationService.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CU72 - WorkflowValidationService unit tests")
class WorkflowValidationServiceTest {

    private WorkflowValidationService service;

    @BeforeEach
    void setUp() {
        service = new WorkflowValidationService();
    }

    private WorkflowNode makeNode(int id, WorkflowNodeType tipo) {
        WorkflowNode n = new WorkflowNode(id);
        n.setTipo(tipo);
        EstadoDeGestion e = new EstadoDeGestion(id);
        e.setNombre("Estado " + id);
        n.setEstadoDeGestion(e);
        return n;
    }

    private WorkflowTransition makeTransition(int id, WorkflowNode origen, WorkflowNode destino) {
        WorkflowTransition t = new WorkflowTransition(id);
        t.setNodoOrigen(origen);
        t.setNodoDestino(destino);
        return t;
    }

    @Nested
    @DisplayName("Valid workflow scenarios")
    class ValidScenarios {

        @Test
        @DisplayName("Should pass for minimal valid workflow: INITIAL → FINAL")
        void shouldPassForMinimalValidWorkflow() {
            WorkflowNode initial = makeNode(1, WorkflowNodeType.INITIAL);
            WorkflowNode finalNode = makeNode(2, WorkflowNodeType.FINAL);
            WorkflowTransition t = makeTransition(1, initial, finalNode);

            ValidationResult result = service.validate(List.of(initial, finalNode), List.of(t));

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should pass for INITIAL → INTERMEDIATE → FINAL")
        void shouldPassForLinearWorkflow() {
            WorkflowNode initial = makeNode(1, WorkflowNodeType.INITIAL);
            WorkflowNode mid = makeNode(2, WorkflowNodeType.INTERMEDIATE);
            WorkflowNode finalNode = makeNode(3, WorkflowNodeType.FINAL);
            WorkflowTransition t1 = makeTransition(1, initial, mid);
            WorkflowTransition t2 = makeTransition(2, mid, finalNode);

            ValidationResult result = service.validate(List.of(initial, mid, finalNode), List.of(t1, t2));

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Invalid workflow scenarios")
    class InvalidScenarios {

        @Test
        @DisplayName("Should fail when no nodes")
        void shouldFailWhenNoNodes() {
            ValidationResult result = service.validate(List.of(), List.of());

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).isNotEmpty();
        }

        @Test
        @DisplayName("Should fail when no INITIAL node")
        void shouldFailWhenNoInitialNode() {
            WorkflowNode mid = makeNode(1, WorkflowNodeType.INTERMEDIATE);
            WorkflowNode finalNode = makeNode(2, WorkflowNodeType.FINAL);
            WorkflowTransition t = makeTransition(1, mid, finalNode);

            ValidationResult result = service.validate(List.of(mid, finalNode), List.of(t));

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("inicial"));
        }

        @Test
        @DisplayName("Should fail when more than one INITIAL node")
        void shouldFailWhenMultipleInitialNodes() {
            WorkflowNode i1 = makeNode(1, WorkflowNodeType.INITIAL);
            WorkflowNode i2 = makeNode(2, WorkflowNodeType.INITIAL);
            WorkflowNode finalNode = makeNode(3, WorkflowNodeType.FINAL);

            ValidationResult result = service.validate(List.of(i1, i2, finalNode), List.of());

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("inicial"));
        }

        @Test
        @DisplayName("Should fail when no FINAL node")
        void shouldFailWhenNoFinalNode() {
            WorkflowNode initial = makeNode(1, WorkflowNodeType.INITIAL);
            WorkflowNode mid = makeNode(2, WorkflowNodeType.INTERMEDIATE);
            WorkflowTransition t = makeTransition(1, initial, mid);

            ValidationResult result = service.validate(List.of(initial, mid), List.of(t));

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("final"));
        }

        @Test
        @DisplayName("Should fail when orphan node unreachable from INITIAL")
        void shouldFailWhenOrphanNodeExists() {
            WorkflowNode initial = makeNode(1, WorkflowNodeType.INITIAL);
            WorkflowNode finalNode = makeNode(2, WorkflowNodeType.FINAL);
            WorkflowNode orphan = makeNode(3, WorkflowNodeType.INTERMEDIATE);
            WorkflowTransition t = makeTransition(1, initial, finalNode);

            ValidationResult result = service.validate(List.of(initial, finalNode, orphan), List.of(t));

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("alcanzable") || e.contains("aislado"));
        }
    }
}
