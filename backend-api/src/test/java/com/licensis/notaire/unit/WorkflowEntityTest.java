package com.licensis.notaire.unit;

import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowNodeType;
import com.licensis.notaire.business.WorkflowTransition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU83"})
@DisplayName("Workflow Entity Tests")
class WorkflowEntityTest {

    @Nested
    @DisplayName("CU83 - WorkflowDefinition entity")
    class WorkflowDefinitionTests {

        @Test
        @DisplayName("Should create workflow definition with required fields")
        void shouldCreateWorkflowDefinitionWithRequiredFields() {
            WorkflowDefinition wf = new WorkflowDefinition();
            wf.setName("Workflow Compraventa");
            wf.setDescription("Workflow estándar para trámites de compraventa");
            wf.setActive(false);

            assertThat(wf.getName()).isEqualTo("Workflow Compraventa");
            assertThat(wf.getDescription()).isEqualTo("Workflow estándar para trámites de compraventa");
            assertThat(wf.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should activate workflow definition")
        void shouldActivateWorkflowDefinition() {
            WorkflowDefinition wf = new WorkflowDefinition();
            wf.setName("Workflow Test");
            wf.setActive(false);

            wf.setActive(true);

            assertThat(wf.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            WorkflowDefinition wf1 = new WorkflowDefinition(1);
            WorkflowDefinition wf2 = new WorkflowDefinition(1);
            WorkflowDefinition wf3 = new WorkflowDefinition(2);

            assertThat(wf1).isEqualTo(wf2);
            assertThat(wf1).isNotEqualTo(wf3);
        }

        @Test
        @DisplayName("Should initialize nodes and transitions collections as empty")
        void shouldInitializeCollectionsAsEmpty() {
            WorkflowDefinition wf = new WorkflowDefinition();

            assertThat(wf.getNodes()).isNotNull();
            assertThat(wf.getNodes()).isEmpty();
            assertThat(wf.getTransitions()).isNotNull();
            assertThat(wf.getTransitions()).isEmpty();
        }
    }

    @Nested
    @DisplayName("CU83 - WorkflowNode entity")
    class WorkflowNodeTests {

        @Test
        @DisplayName("Should create initial node linked to a workflow and status")
        void shouldCreateInitialNodeLinkedToWorkflowAndStatus() {
            WorkflowDefinition wf = new WorkflowDefinition(1);
            ManagementStatus status = new ManagementStatus(10);
            status.setName("Iniciado");

            WorkflowNode node = new WorkflowNode();
            node.setWorkflowDefinition(wf);
            node.setManagementStatus(status);
            node.setType(WorkflowNodeType.INITIAL);
            node.setPositionX(100.0f);
            node.setPositionY(200.0f);

            assertThat(node.getWorkflowDefinition()).isEqualTo(wf);
            assertThat(node.getManagementStatus().getName()).isEqualTo("Iniciado");
            assertThat(node.getType()).isEqualTo(WorkflowNodeType.INITIAL);
            assertThat(node.getPositionX()).isEqualTo(100.0f);
            assertThat(node.getPositionY()).isEqualTo(200.0f);
        }

        @Test
        @DisplayName("Should distinguish node types")
        void shouldDistinguishNodeTypes() {
            WorkflowNode initial = new WorkflowNode();
            initial.setType(WorkflowNodeType.INITIAL);

            WorkflowNode intermediate = new WorkflowNode();
            intermediate.setType(WorkflowNodeType.INTERMEDIATE);

            WorkflowNode finalNode = new WorkflowNode();
            finalNode.setType(WorkflowNodeType.FINAL);

            assertThat(initial.getType()).isEqualTo(WorkflowNodeType.INITIAL);
            assertThat(intermediate.getType()).isEqualTo(WorkflowNodeType.INTERMEDIATE);
            assertThat(finalNode.getType()).isEqualTo(WorkflowNodeType.FINAL);
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            WorkflowNode n1 = new WorkflowNode(1);
            WorkflowNode n2 = new WorkflowNode(1);
            WorkflowNode n3 = new WorkflowNode(2);

            assertThat(n1).isEqualTo(n2);
            assertThat(n1).isNotEqualTo(n3);
        }
    }

    @Nested
    @DisplayName("CU83 - WorkflowTransition entity")
    class WorkflowTransitionTests {

        @Test
        @DisplayName("Should create transition between two nodes")
        void shouldCreateTransitionBetweenTwoNodes() {
            WorkflowDefinition wf = new WorkflowDefinition(1);
            WorkflowNode origin = new WorkflowNode(1);
            WorkflowNode destination = new WorkflowNode(2);

            WorkflowTransition transition = new WorkflowTransition();
            transition.setWorkflowDefinition(wf);
            transition.setOriginNode(origin);
            transition.setDestinationNode(destination);
            transition.setDescription("Avance de status");

            assertThat(transition.getWorkflowDefinition()).isEqualTo(wf);
            assertThat(transition.getOriginNode()).isEqualTo(origin);
            assertThat(transition.getDestinationNode()).isEqualTo(destination);
            assertThat(transition.getDescription()).isEqualTo("Avance de status");
        }

        @Test
        @DisplayName("Should allow optional condition on transition")
        void shouldAllowOptionalConditionOnTransition() {
            WorkflowTransition transition = new WorkflowTransition();
            transition.setCondition("status.documentosCompletos == true");

            assertThat(transition.getCondition()).isEqualTo("status.documentosCompletos == true");
        }

        @Test
        @DisplayName("Should allow null condition when no restriction")
        void shouldAllowNullConditionWhenNoRestriction() {
            WorkflowTransition transition = new WorkflowTransition();

            assertThat(transition.getCondition()).isNull();
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            WorkflowTransition t1 = new WorkflowTransition(1);
            WorkflowTransition t2 = new WorkflowTransition(1);
            WorkflowTransition t3 = new WorkflowTransition(2);

            assertThat(t1).isEqualTo(t2);
            assertThat(t1).isNotEqualTo(t3);
        }
    }
}
