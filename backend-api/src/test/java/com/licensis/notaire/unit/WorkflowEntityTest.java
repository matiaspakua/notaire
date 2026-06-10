package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowNodeType;
import com.licensis.notaire.negocio.WorkflowTransition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU70", "CU71", "CU72"})
@DisplayName("Workflow Entity Tests")
class WorkflowEntityTest {

    @Nested
    @DisplayName("CU70 - WorkflowDefinition entity")
    class WorkflowDefinitionTests {

        @Test
        @DisplayName("Should create workflow definition with required fields")
        void shouldCreateWorkflowDefinitionWithRequiredFields() {
            WorkflowDefinition wf = new WorkflowDefinition();
            wf.setNombre("Workflow Compraventa");
            wf.setDescripcion("Workflow estándar para trámites de compraventa");
            wf.setActivo(false);

            assertThat(wf.getNombre()).isEqualTo("Workflow Compraventa");
            assertThat(wf.getDescripcion()).isEqualTo("Workflow estándar para trámites de compraventa");
            assertThat(wf.isActivo()).isFalse();
        }

        @Test
        @DisplayName("Should activate workflow definition")
        void shouldActivateWorkflowDefinition() {
            WorkflowDefinition wf = new WorkflowDefinition();
            wf.setNombre("Workflow Test");
            wf.setActivo(false);

            wf.setActivo(true);

            assertThat(wf.isActivo()).isTrue();
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
    @DisplayName("CU70 - WorkflowNode entity")
    class WorkflowNodeTests {

        @Test
        @DisplayName("Should create initial node linked to a workflow and estado")
        void shouldCreateInitialNodeLinkedToWorkflowAndEstado() {
            WorkflowDefinition wf = new WorkflowDefinition(1);
            EstadoDeGestion estado = new EstadoDeGestion(10);
            estado.setNombre("Iniciado");

            WorkflowNode node = new WorkflowNode();
            node.setWorkflowDefinition(wf);
            node.setEstadoDeGestion(estado);
            node.setTipo(WorkflowNodeType.INITIAL);
            node.setPosicionX(100.0f);
            node.setPosicionY(200.0f);

            assertThat(node.getWorkflowDefinition()).isEqualTo(wf);
            assertThat(node.getEstadoDeGestion().getNombre()).isEqualTo("Iniciado");
            assertThat(node.getTipo()).isEqualTo(WorkflowNodeType.INITIAL);
            assertThat(node.getPosicionX()).isEqualTo(100.0f);
            assertThat(node.getPosicionY()).isEqualTo(200.0f);
        }

        @Test
        @DisplayName("Should distinguish node types")
        void shouldDistinguishNodeTypes() {
            WorkflowNode initial = new WorkflowNode();
            initial.setTipo(WorkflowNodeType.INITIAL);

            WorkflowNode intermediate = new WorkflowNode();
            intermediate.setTipo(WorkflowNodeType.INTERMEDIATE);

            WorkflowNode finalNode = new WorkflowNode();
            finalNode.setTipo(WorkflowNodeType.FINAL);

            assertThat(initial.getTipo()).isEqualTo(WorkflowNodeType.INITIAL);
            assertThat(intermediate.getTipo()).isEqualTo(WorkflowNodeType.INTERMEDIATE);
            assertThat(finalNode.getTipo()).isEqualTo(WorkflowNodeType.FINAL);
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
    @DisplayName("CU71 - WorkflowTransition entity")
    class WorkflowTransitionTests {

        @Test
        @DisplayName("Should create transition between two nodes")
        void shouldCreateTransitionBetweenTwoNodes() {
            WorkflowDefinition wf = new WorkflowDefinition(1);
            WorkflowNode origen = new WorkflowNode(1);
            WorkflowNode destino = new WorkflowNode(2);

            WorkflowTransition transition = new WorkflowTransition();
            transition.setWorkflowDefinition(wf);
            transition.setNodoOrigen(origen);
            transition.setNodoDestino(destino);
            transition.setDescripcion("Avance de estado");

            assertThat(transition.getWorkflowDefinition()).isEqualTo(wf);
            assertThat(transition.getNodoOrigen()).isEqualTo(origen);
            assertThat(transition.getNodoDestino()).isEqualTo(destino);
            assertThat(transition.getDescripcion()).isEqualTo("Avance de estado");
        }

        @Test
        @DisplayName("Should allow optional condition on transition")
        void shouldAllowOptionalConditionOnTransition() {
            WorkflowTransition transition = new WorkflowTransition();
            transition.setCondicion("estado.documentosCompletos == true");

            assertThat(transition.getCondicion()).isEqualTo("estado.documentosCompletos == true");
        }

        @Test
        @DisplayName("Should allow null condition when no restriction")
        void shouldAllowNullConditionWhenNoRestriction() {
            WorkflowTransition transition = new WorkflowTransition();

            assertThat(transition.getCondicion()).isNull();
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
