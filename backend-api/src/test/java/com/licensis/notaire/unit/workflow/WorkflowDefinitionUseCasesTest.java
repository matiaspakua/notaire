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

import com.licensis.notaire.application.port.in.workflow.ManageWorkflowDefinitionsUseCase.SaveWorkflowDefinitionCommand;
import com.licensis.notaire.application.port.out.workflow.WorkflowDefinitionCatalogPort;
import com.licensis.notaire.application.usecase.workflow.WorkflowDefinitionManagementService;
import com.licensis.notaire.application.usecase.workflow.WorkflowDefinitionQueryService;
import com.licensis.notaire.domain.workflow.WorkflowDefinition;
import com.licensis.notaire.domain.workflow.WorkflowDefinitionInUseException;
import com.licensis.notaire.domain.workflow.WorkflowDefinitionNotFoundException;

@DisplayName("Workflow definition use cases (issue #1728)")
class WorkflowDefinitionUseCasesTest {

    private InMemoryCatalog catalog;
    private WorkflowDefinitionManagementService manage;
    private WorkflowDefinitionQueryService query;

    @BeforeEach
    void setUp() {
        catalog = new InMemoryCatalog();
        manage = new WorkflowDefinitionManagementService(catalog);
        query = new WorkflowDefinitionQueryService(catalog);
    }

    @Test
    @DisplayName("create and list round-trip")
    void createAndList() {
        WorkflowDefinition created =
                manage.create(new SaveWorkflowDefinitionCommand("Sale", "desc", false, null));
        assertThat(created.id()).isNotNull();
        assertThat(query.findAll()).extracting(WorkflowDefinition::name).containsExactly("Sale");
    }

    @Test
    @DisplayName("update changes fields")
    void update() {
        WorkflowDefinition created =
                manage.create(new SaveWorkflowDefinitionCommand("Sale", "desc", false, null));
        manage.update(created.id(), new SaveWorkflowDefinitionCommand("Sale 2", "new", true, 0));
        assertThat(query.findById(created.id()))
                .hasValueSatisfying(wf -> {
                    assertThat(wf.name()).isEqualTo("Sale 2");
                    assertThat(wf.active()).isTrue();
                });
    }

    @Test
    @DisplayName("delete without nodes succeeds")
    void deleteFree() {
        WorkflowDefinition created =
                manage.create(new SaveWorkflowDefinitionCommand("Sale", null, false, null));
        manage.delete(created.id());
        assertThat(query.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("delete with nodes is refused")
    void deleteWithNodes() {
        WorkflowDefinition created =
                manage.create(new SaveWorkflowDefinitionCommand("Sale", null, false, null));
        catalog.markHasNodes(created.id());
        assertThatThrownBy(() -> manage.delete(created.id()))
                .isInstanceOf(WorkflowDefinitionInUseException.class)
                .hasMessage(WorkflowDefinitionInUseException.DELETE_MESSAGE);
    }

    @Test
    @DisplayName("missing id on update is refused")
    void updateMissing() {
        assertThatThrownBy(
                        () -> manage.update(99, new SaveWorkflowDefinitionCommand("X", null, false, null)))
                .isInstanceOf(WorkflowDefinitionNotFoundException.class);
    }

    static final class InMemoryCatalog implements WorkflowDefinitionCatalogPort {
        private final AtomicInteger seq = new AtomicInteger(1);
        private final Map<Integer, WorkflowDefinition> rows = new HashMap<>();
        private final Set<Integer> withNodes = new HashSet<>();

        void markHasNodes(int id) {
            withNodes.add(id);
        }

        @Override
        public List<WorkflowDefinition> findAll() {
            return List.copyOf(rows.values());
        }

        @Override
        public Optional<WorkflowDefinition> findById(int id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override
        public boolean existsById(int id) {
            return rows.containsKey(id);
        }

        @Override
        public boolean hasNodes(int id) {
            return withNodes.contains(id);
        }

        @Override
        public WorkflowDefinition save(WorkflowDefinition definition) {
            if (definition.id() == null) {
                WorkflowDefinition stored = definition.withIdentity(seq.getAndIncrement(), 0);
                rows.put(stored.id(), stored);
                return stored;
            }
            rows.put(definition.id(), definition);
            return definition;
        }

        @Override
        public boolean deleteById(int id) {
            return rows.remove(id) != null;
        }
    }
}
