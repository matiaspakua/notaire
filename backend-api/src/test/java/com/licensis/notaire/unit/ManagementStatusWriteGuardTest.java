package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.licensis.notaire.application.usecase.management.ManagementStatusWriteGuard;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowNodeType;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU83", "CU02", "CU53"})
@ExtendWith(MockitoExtension.class)
@DisplayName("ManagementStatusWriteGuard — CU83 status write enforcement")
class ManagementStatusWriteGuardTest {

    @Mock
    private WorkflowNodeRepository workflowNodeRepository;

    private ManagementStatusWriteGuard guard;

    @BeforeEach
    void setUp() {
        guard = new ManagementStatusWriteGuard(workflowNodeRepository);
    }

    @Test
    @DisplayName("Should allow same-status update")
    void shouldAllowSameStatusUpdate() {
        assertThatCode(() -> guard.rejectStatusMutationOnUpdate(1, 1)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow update when status field is omitted")
    void shouldAllowOmittedStatusOnUpdate() {
        assertThatCode(() -> guard.rejectStatusMutationOnUpdate(1, null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject status mutation on update")
    void shouldRejectStatusMutationOnUpdate() {
        assertThatThrownBy(() -> guard.rejectStatusMutationOnUpdate(1, 2))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("transition");
    }

    @Test
    @DisplayName("Should reject first status assignment via update")
    void shouldRejectFirstStatusAssignmentViaUpdate() {
        assertThatThrownBy(() -> guard.rejectStatusMutationOnUpdate(null, 2))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("transition");
    }

    @Test
    @DisplayName("Should allow initial status when it is the start node")
    void shouldAllowInitialStartNodeStatus() {
        WorkflowDefinition definition = definition(10);
        ManagementStatus start = status(1);
        ManagementStatus next = status(2);
        when(workflowNodeRepository.findByWorkflowDefinitionId(10))
                .thenReturn(List.of(node(definition, start, WorkflowNodeType.INITIAL),
                        node(definition, next, WorkflowNodeType.FINAL)));

        assertThatCode(() -> guard.validateInitialStatus(definition, start)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject initial status outside the workflow")
    void shouldRejectInitialStatusOutsideWorkflow() {
        WorkflowDefinition definition = definition(10);
        ManagementStatus start = status(1);
        ManagementStatus outside = status(99);
        when(workflowNodeRepository.findByWorkflowDefinitionId(10))
                .thenReturn(List.of(node(definition, start, WorkflowNodeType.INITIAL)));

        assertThatThrownBy(() -> guard.validateInitialStatus(definition, outside))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("start node");
    }

    @Test
    @DisplayName("Should reject non-start node when INITIAL nodes exist")
    void shouldRejectNonStartWhenInitialExists() {
        WorkflowDefinition definition = definition(10);
        ManagementStatus start = status(1);
        ManagementStatus next = status(2);
        when(workflowNodeRepository.findByWorkflowDefinitionId(10))
                .thenReturn(List.of(node(definition, start, WorkflowNodeType.INITIAL),
                        node(definition, next, WorkflowNodeType.FINAL)));

        assertThatThrownBy(() -> guard.validateInitialStatus(definition, next))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("start node");
    }

    @Test
    @DisplayName("Should skip validation when no workflow is present")
    void shouldSkipWhenNoWorkflow() {
        assertThatCode(() -> guard.validateInitialStatus(null, status(1))).doesNotThrowAnyException();
    }

    private static WorkflowDefinition definition(int id) {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId(id);
        return definition;
    }

    private static ManagementStatus status(int id) {
        ManagementStatus status = new ManagementStatus();
        status.setIdManagementStatus(id);
        status.setName("Status-" + id);
        return status;
    }

    private static WorkflowNode node(WorkflowDefinition definition, ManagementStatus status,
            WorkflowNodeType type) {
        WorkflowNode node = new WorkflowNode();
        node.setWorkflowDefinition(definition);
        node.setManagementStatus(status);
        node.setType(type);
        return node;
    }
}
