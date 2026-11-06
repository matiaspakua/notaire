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

import com.licensis.notaire.application.port.in.workflow.ManageWorkflowDefinitionsUseCase;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowDefinitionsUseCase;
import com.licensis.notaire.config.GlobalExceptionHandler;
import com.licensis.notaire.domain.workflow.WorkflowDefinitionInUseException;
import com.licensis.notaire.domain.workflow.WorkflowDefinitionNotFoundException;

@DisplayName("WorkflowDefinitionExceptionHandler maps domain exceptions (#577)")
class WorkflowDefinitionExceptionHandlerTest {

    private final ManageWorkflowDefinitionsUseCase manage = mock(ManageWorkflowDefinitionsUseCase.class);
    private final MockMvc mvc = standaloneSetup(
                    new WorkflowDefinitionController(mock(QueryWorkflowDefinitionsUseCase.class), manage))
            .setControllerAdvice(new WorkflowDefinitionExceptionHandler(), new GlobalExceptionHandler())
            .build();

    @Test
    @DisplayName("delete InUse returns 409 error body")
    void deleteInUseReturnsConflict() throws Exception {
        doThrow(WorkflowDefinitionInUseException.cannotDelete()).when(manage).delete(1);

        mvc.perform(delete("/api/v1/workflow-definition/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(WorkflowDefinitionInUseException.DELETE_MESSAGE));
    }

    @Test
    @DisplayName("delete NotFound returns 404")
    void deleteNotFoundReturns404() throws Exception {
        doThrow(new WorkflowDefinitionNotFoundException(9)).when(manage).delete(9);

        mvc.perform(delete("/api/v1/workflow-definition/9")).andExpect(status().isNotFound());
    }
}
