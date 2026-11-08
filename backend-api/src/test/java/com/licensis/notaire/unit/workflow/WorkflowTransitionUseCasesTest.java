package com.licensis.notaire.unit.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.in.workflow.ManageWorkflowTransitionsUseCase.CreateWorkflowTransitionCommand;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowTransitionsUseCase.UpdateWorkflowTransitionCommand;
import com.licensis.notaire.application.port.out.workflow.WorkflowDefinitionCatalogPort;
import com.licensis.notaire.application.port.out.workflow.WorkflowNodeCatalogPort;
import com.licensis.notaire.application.port.out.workflow.WorkflowTransitionCatalogPort;
import com.licensis.notaire.application.usecase.workflow.WorkflowTransitionManagementService;
import com.licensis.notaire.application.usecase.workflow.WorkflowTransitionQueryService;
import com.licensis.notaire.domain.workflow.WorkflowDefinition;
import com.licensis.notaire.domain.workflow.WorkflowNode;
import com.licensis.notaire.domain.workflow.WorkflowTransition;
import com.licensis.notaire.domain.workflow.WorkflowTransitionInvalidException;
import com.licensis.notaire.domain.workflow.WorkflowTransitionNotFoundException;

@DisplayName("Workflow transition use cases (issue #1730)")
class WorkflowTransitionUseCasesTest {

    private InMemoryTransitionCatalog transitions;
    private Set<Integer> knownNodes;
    private WorkflowTransitionManagementService manage;
    private WorkflowTransitionQueryService query;

    @BeforeEach
    void setUp() {
        transitions = new InMemoryTransitionCatalog();
        knownNodes = new HashSet<>(Set.of(1, 2, 3));
        WorkflowDefinitionCatalogPort definitions = new WorkflowDefinitionCatalogPort() {
            @Override
            public List<WorkflowDefinition> findAll() {
                return List.of();
            }

            @Override
            public Optional<WorkflowDefinition> findById(int id) {
                return id == 1 ? Optional.of(WorkflowDefinition.restore(1, "WF", null, false, 0)) : Optional.empty();
            }

            @Override
            public boolean existsById(int id) {
                return id == 1;
            }

            @Override
            public boolean hasNodes(int id) {
                return false;
            }

            @Override
            public WorkflowDefinition save(WorkflowDefinition definition) {
                return definition;
            }

            @Override
            public boolean deleteById(int id) {
                return false;
            }
        };
        WorkflowNodeCatalogPort nodes = new WorkflowNodeCatalogPort() {
            @Override
            public List<WorkflowNode> findByWorkflowDefinitionId(int workflowDefinitionId) {
                return List.of();
            }

            @Override
            public Optional<WorkflowNode> findById(int id) {
                return Optional.empty();
            }

            @Override
            public boolean existsById(int id) {
                return knownNodes.contains(id);
            }

            @Override
            public boolean hasTransitions(int id) {
                return false;
            }

            @Override
            public WorkflowNode save(WorkflowNode node) {
                return node;
            }

            @Override
            public boolean deleteById(int id) {
                return false;
            }
        };
        manage = new WorkflowTransitionManagementService(transitions, definitions, nodes);
        query = new WorkflowTransitionQueryService(transitions);
    }

    @Test
    @DisplayName("create and list by workflow")
    void createAndList() {
        WorkflowTransition created = manage.create(
                new CreateWorkflowTransitionCommand(1, 1, 2, null, "Start → Review"));
        assertThat(created.id()).isNotNull();
        assertThat(query.findByWorkflowDefinitionId(1)).hasSize(1);
    }

    @Test
    @DisplayName("missing workflow on create is refused")
    void missingWorkflow() {
        assertThatThrownBy(() -> manage.create(
                        new CreateWorkflowTransitionCommand(99, 1, 2, null, null)))
                .isInstanceOf(WorkflowTransitionInvalidException.class);
    }

    @Test
    @DisplayName("missing origin node on create is refused")
    void missingOrigin() {
        assertThatThrownBy(() -> manage.create(
                        new CreateWorkflowTransitionCommand(1, 99, 2, null, null)))
                .isInstanceOf(WorkflowTransitionInvalidException.class);
    }

    @Test
    @DisplayName("update changes description and nodes")
    void update() {
        WorkflowTransition created = manage.create(
                new CreateWorkflowTransitionCommand(1, 1, 2, "cond", "old"));
        WorkflowTransition updated = manage.update(
                created.id(), new UpdateWorkflowTransitionCommand(1, 3, "new-cond", "new"));
        assertThat(updated.destinationNodeId()).isEqualTo(3);
        assertThat(updated.description()).isEqualTo("new");
        assertThat(query.findById(created.id())).hasValueSatisfying(t -> assertThat(t.condition()).isEqualTo("new-cond"));
    }

    @Test
    @DisplayName("update missing transition is refused")
    void updateMissing() {
        assertThatThrownBy(() -> manage.update(99, new UpdateWorkflowTransitionCommand(1, 2, null, null)))
                .isInstanceOf(WorkflowTransitionNotFoundException.class);
    }

    @Test
    @DisplayName("delete succeeds")
    void delete() {
        WorkflowTransition created = manage.create(
                new CreateWorkflowTransitionCommand(1, 1, 2, null, "x"));
        manage.delete(created.id());
        assertThat(query.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("delete missing is refused")
    void deleteMissing() {
        assertThatThrownBy(() -> manage.delete(99)).isInstanceOf(WorkflowTransitionNotFoundException.class);
    }

    private static final class InMemoryTransitionCatalog implements WorkflowTransitionCatalogPort {
        private final Map<Integer, WorkflowTransition> byId = new HashMap<>();
        private final AtomicInteger seq = new AtomicInteger(1);

        @Override
        public List<WorkflowTransition> findByWorkflowDefinitionId(int workflowDefinitionId) {
            return byId.values().stream()
                    .filter(t -> Integer.valueOf(workflowDefinitionId).equals(t.workflowDefinitionId()))
                    .toList();
        }

        @Override
        public Optional<WorkflowTransition> findById(int id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public boolean existsById(int id) {
            return byId.containsKey(id);
        }

        @Override
        public WorkflowTransition save(WorkflowTransition transition) {
            int id = transition.id() == null ? seq.getAndIncrement() : transition.id();
            WorkflowTransition stored = transition.withIdentity(id, transition.version());
            byId.put(id, stored);
            return stored;
        }

        @Override
        public boolean deleteById(int id) {
            return byId.remove(id) != null;
        }
    }
}
