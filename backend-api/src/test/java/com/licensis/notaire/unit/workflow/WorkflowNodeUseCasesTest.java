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

import com.licensis.notaire.application.port.in.workflow.ManageWorkflowNodesUseCase.CreateWorkflowNodeCommand;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowNodesUseCase.UpdateWorkflowNodeCommand;
import com.licensis.notaire.application.port.out.management.ManagementStatusCatalogPort;
import com.licensis.notaire.application.port.out.workflow.WorkflowDefinitionCatalogPort;
import com.licensis.notaire.application.port.out.workflow.WorkflowNodeCatalogPort;
import com.licensis.notaire.application.usecase.workflow.WorkflowNodeManagementService;
import com.licensis.notaire.application.usecase.workflow.WorkflowNodeQueryService;
import com.licensis.notaire.domain.management.ManagementStatus;
import com.licensis.notaire.domain.workflow.WorkflowDefinition;
import com.licensis.notaire.domain.workflow.WorkflowNode;
import com.licensis.notaire.domain.workflow.WorkflowNodeInUseException;
import com.licensis.notaire.domain.workflow.WorkflowNodeInvalidException;
import com.licensis.notaire.domain.workflow.WorkflowNodeKind;
import com.licensis.notaire.domain.workflow.WorkflowNodeNotFoundException;

@DisplayName("Workflow node use cases (issue #1729)")
class WorkflowNodeUseCasesTest {

    private InMemoryNodeCatalog nodes;
    private WorkflowNodeManagementService manage;
    private WorkflowNodeQueryService query;

    @BeforeEach
    void setUp() {
        nodes = new InMemoryNodeCatalog();
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
        ManagementStatusCatalogPort statuses = new ManagementStatusCatalogPort() {
            @Override
            public List<ManagementStatus> findAll() {
                return List.of();
            }

            @Override
            public List<ManagementStatus> findByNameContaining(String text) {
                return List.of();
            }

            @Override
            public Optional<ManagementStatus> findById(int id) {
                return id == 10 ? Optional.of(ManagementStatus.restore(10, "Started", null, 0)) : Optional.empty();
            }

            @Override
            public ManagementStatus save(ManagementStatus status) {
                return status;
            }

            @Override
            public boolean deleteById(int id) {
                return false;
            }
        };
        manage = new WorkflowNodeManagementService(nodes, definitions, statuses);
        query = new WorkflowNodeQueryService(nodes);
    }

    @Test
    @DisplayName("create and list by workflow")
    void createAndList() {
        WorkflowNode created = manage.create(
                new CreateWorkflowNodeCommand(1, 10, WorkflowNodeKind.INITIAL, 1f, 2f));
        assertThat(created.id()).isNotNull();
        assertThat(query.findByWorkflowDefinitionId(1)).hasSize(1);
    }

    @Test
    @DisplayName("missing workflow on create is refused")
    void missingWorkflow() {
        assertThatThrownBy(() -> manage.create(
                        new CreateWorkflowNodeCommand(99, 10, WorkflowNodeKind.INITIAL, null, null)))
                .isInstanceOf(WorkflowNodeInvalidException.class);
    }

    @Test
    @DisplayName("update changes position")
    void update() {
        WorkflowNode created = manage.create(
                new CreateWorkflowNodeCommand(1, 10, WorkflowNodeKind.INTERMEDIATE, 0f, 0f));
        manage.update(created.id(), new UpdateWorkflowNodeCommand(null, 5f, null));
        assertThat(query.findById(created.id())).hasValueSatisfying(n -> assertThat(n.positionX()).isEqualTo(5f));
    }

    @Test
    @DisplayName("delete without transitions succeeds")
    void deleteFree() {
        WorkflowNode created = manage.create(
                new CreateWorkflowNodeCommand(1, 10, WorkflowNodeKind.FINAL, null, null));
        manage.delete(created.id());
        assertThat(query.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("delete with transitions is refused")
    void deleteWithTransitions() {
        WorkflowNode created = manage.create(
                new CreateWorkflowNodeCommand(1, 10, WorkflowNodeKind.INITIAL, null, null));
        nodes.markHasTransitions(created.id());
        assertThatThrownBy(() -> manage.delete(created.id()))
                .isInstanceOf(WorkflowNodeInUseException.class)
                .hasMessage(WorkflowNodeInUseException.DELETE_MESSAGE);
    }

    @Test
    @DisplayName("missing id on update is refused")
    void updateMissing() {
        assertThatThrownBy(() -> manage.update(99, new UpdateWorkflowNodeCommand(WorkflowNodeKind.FINAL, null, null)))
                .isInstanceOf(WorkflowNodeNotFoundException.class);
    }

    static final class InMemoryNodeCatalog implements WorkflowNodeCatalogPort {
        private final AtomicInteger seq = new AtomicInteger(1);
        private final Map<Integer, WorkflowNode> rows = new HashMap<>();
        private final Set<Integer> withTransitions = new HashSet<>();

        void markHasTransitions(int id) {
            withTransitions.add(id);
        }

        @Override
        public List<WorkflowNode> findByWorkflowDefinitionId(int workflowDefinitionId) {
            return rows.values().stream()
                    .filter(n -> Integer.valueOf(workflowDefinitionId).equals(n.workflowDefinitionId()))
                    .toList();
        }

        @Override
        public Optional<WorkflowNode> findById(int id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override
        public boolean existsById(int id) {
            return rows.containsKey(id);
        }

        @Override
        public boolean hasTransitions(int id) {
            return withTransitions.contains(id);
        }

        @Override
        public WorkflowNode save(WorkflowNode node) {
            if (node.id() == null) {
                WorkflowNode stored = node.withIdentity(seq.getAndIncrement(), 0);
                rows.put(stored.id(), stored);
                return stored;
            }
            rows.put(node.id(), node);
            return node;
        }

        @Override
        public boolean deleteById(int id) {
            return rows.remove(id) != null;
        }
    }
}
