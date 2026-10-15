package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.usecase.workflow.WorkflowValidationService;
import com.licensis.notaire.application.usecase.workflow.WorkflowValidationService.ValidationResult;
import com.licensis.notaire.domain.workflow.WorkflowNode;
import com.licensis.notaire.domain.workflow.WorkflowNodeKind;
import com.licensis.notaire.domain.workflow.WorkflowTransition;

@DisplayName("CU72 - WorkflowValidationService unit tests (domain types, issue #1770)")
class WorkflowValidationServiceTest {

    private WorkflowValidationService service;

    @BeforeEach
    void setUp() {
        service = new WorkflowValidationService();
    }

    private WorkflowNode makeNode(int id, WorkflowNodeKind kind, String statusName) {
        return WorkflowNode.restore(id, 1, id, statusName, kind, 0f, 0f, 0);
    }

    private WorkflowTransition makeTransition(int id, int originId, int destinationId) {
        return WorkflowTransition.restore(id, 1, originId, destinationId, null, null, 0);
    }

    @Nested
    @DisplayName("Valid workflow scenarios")
    class ValidScenarios {

        @Test
        @DisplayName("Should pass for minimal valid workflow: INITIAL → FINAL")
        void shouldPassForMinimalValidWorkflow() {
            ValidationResult result = service.validate(
                    List.of(
                            makeNode(1, WorkflowNodeKind.INITIAL, "Start"),
                            makeNode(2, WorkflowNodeKind.FINAL, "End")),
                    List.of(makeTransition(1, 1, 2)));

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should pass for INITIAL → INTERMEDIATE → FINAL")
        void shouldPassForLinearWorkflow() {
            ValidationResult result = service.validate(
                    List.of(
                            makeNode(1, WorkflowNodeKind.INITIAL, "A"),
                            makeNode(2, WorkflowNodeKind.INTERMEDIATE, "B"),
                            makeNode(3, WorkflowNodeKind.FINAL, "C")),
                    List.of(makeTransition(1, 1, 2), makeTransition(2, 2, 3)));

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
            assertThat(result.getErrors()).contains("The workflow has no nodes.");
        }

        @Test
        @DisplayName("Should fail when no INITIAL node")
        void shouldFailWhenNoInitialNode() {
            ValidationResult result = service.validate(
                    List.of(
                            makeNode(1, WorkflowNodeKind.INTERMEDIATE, "Mid"),
                            makeNode(2, WorkflowNodeKind.FINAL, "End")),
                    List.of(makeTransition(1, 1, 2)));

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("initial"));
        }

        @Test
        @DisplayName("Should fail when multiple INITIAL nodes")
        void shouldFailWhenMultipleInitialNodes() {
            ValidationResult result = service.validate(
                    List.of(
                            makeNode(1, WorkflowNodeKind.INITIAL, "A"),
                            makeNode(2, WorkflowNodeKind.INITIAL, "B"),
                            makeNode(3, WorkflowNodeKind.FINAL, "C")),
                    List.of());

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("more than one initial node"));
        }

        @Test
        @DisplayName("Should fail when no FINAL node")
        void shouldFailWhenNoFinalNode() {
            ValidationResult result = service.validate(
                    List.of(
                            makeNode(1, WorkflowNodeKind.INITIAL, "Start"),
                            makeNode(2, WorkflowNodeKind.INTERMEDIATE, "Mid")),
                    List.of(makeTransition(1, 1, 2)));

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("final node"));
        }

        @Test
        @DisplayName("Should fail when nodes are unreachable from INITIAL")
        void shouldFailWhenUnreachableNodes() {
            ValidationResult result = service.validate(
                    List.of(
                            makeNode(1, WorkflowNodeKind.INITIAL, "Start"),
                            makeNode(2, WorkflowNodeKind.FINAL, "End"),
                            makeNode(3, WorkflowNodeKind.FINAL, "Isolated")),
                    List.of(makeTransition(1, 1, 2)));

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("unreachable") && e.contains("Isolated"));
        }
    }
}
