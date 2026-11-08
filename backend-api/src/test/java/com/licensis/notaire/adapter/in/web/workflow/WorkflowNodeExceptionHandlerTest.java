package com.licensis.notaire.adapter.in.web.workflow;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import com.licensis.notaire.application.port.in.workflow.ManageWorkflowNodesUseCase;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowNodesUseCase;
import com.licensis.notaire.config.GlobalExceptionHandler;
import com.licensis.notaire.domain.workflow.WorkflowNodeInUseException;
import com.licensis.notaire.domain.workflow.WorkflowNodeNotFoundException;

@DisplayName("WorkflowNodeExceptionHandler maps domain exceptions (#577)")
class WorkflowNodeExceptionHandlerTest {

    private final ManageWorkflowNodesUseCase manage = mock(ManageWorkflowNodesUseCase.class);
    private final MockMvc mvc = standaloneSetup(
                    new WorkflowNodeController(mock(QueryWorkflowNodesUseCase.class), manage))
            .setControllerAdvice(new WorkflowNodeExceptionHandler(), new GlobalExceptionHandler())
            .build();

    @Test
    @DisplayName("delete InUse returns 409 error body")
    void deleteInUseReturnsConflict() throws Exception {
        doThrow(WorkflowNodeInUseException.cannotDelete()).when(manage).delete(1);

        mvc.perform(delete("/api/v1/workflow-node/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(WorkflowNodeInUseException.DELETE_MESSAGE));
    }

    @Test
    @DisplayName("delete NotFound returns 404")
    void deleteNotFoundReturns404() throws Exception {
        doThrow(new WorkflowNodeNotFoundException(9)).when(manage).delete(9);

        mvc.perform(delete("/api/v1/workflow-node/9")).andExpect(status().isNotFound());
    }
}
