package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.licensis.notaire.application.port.in.workflow.ValidateWorkflowUseCase.Result;
import com.licensis.notaire.application.port.out.workflow.WorkflowDefinitionCatalogPort;
import com.licensis.notaire.application.port.out.workflow.WorkflowNodeCatalogPort;
import com.licensis.notaire.application.port.out.workflow.WorkflowTransitionCatalogPort;
import com.licensis.notaire.application.usecase.workflow.ValidateWorkflowService;
import com.licensis.notaire.application.usecase.workflow.WorkflowValidationService;
import com.licensis.notaire.domain.workflow.WorkflowNode;
import com.licensis.notaire.domain.workflow.WorkflowNodeKind;
import com.licensis.notaire.domain.workflow.WorkflowTransition;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidateWorkflowService (issue #1770)")
class ValidateWorkflowServiceTest {

    @Mock
    private WorkflowDefinitionCatalogPort definitions;
    @Mock
    private WorkflowNodeCatalogPort nodes;
    @Mock
    private WorkflowTransitionCatalogPort transitions;

    @Test
    @DisplayName("returns NotFound when definition missing")
    void notFound() {
        when(definitions.existsById(99)).thenReturn(false);
        ValidateWorkflowService service =
                new ValidateWorkflowService(definitions, nodes, transitions, new WorkflowValidationService());
        assertThat(service.validate(99)).isInstanceOf(Result.NotFound.class);
    }

    @Test
    @DisplayName("returns Ok for a valid graph")
    void okValid() {
        when(definitions.existsById(1)).thenReturn(true);
        when(nodes.findByWorkflowDefinitionId(1))
                .thenReturn(List.of(
                        WorkflowNode.restore(1, 1, 10, "Start", WorkflowNodeKind.INITIAL, 0f, 0f, 0),
                        WorkflowNode.restore(2, 1, 11, "End", WorkflowNodeKind.FINAL, 0f, 0f, 0)));
        when(transitions.findByWorkflowDefinitionId(1))
                .thenReturn(List.of(WorkflowTransition.restore(1, 1, 1, 2, null, null, 0)));
        ValidateWorkflowService service =
                new ValidateWorkflowService(definitions, nodes, transitions, new WorkflowValidationService());
        Result result = service.validate(1);
        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok ok = (Result.Ok) result;
        assertThat(ok.valid()).isTrue();
        assertThat(ok.errors()).isEmpty();
    }
}
