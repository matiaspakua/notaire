package com.licensis.notaire.adapter.in.web.management;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import com.licensis.notaire.application.port.in.document.ExternalEntityDocumentUseCase;
import com.licensis.notaire.application.port.in.management.ManageManagementsUseCase;
import com.licensis.notaire.application.port.in.management.QueryManagementsUseCase;
import com.licensis.notaire.application.port.in.management.TransitionManagementUseCase;
import com.licensis.notaire.application.port.in.workflow.DocumentReentryUseCase;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowTraceUseCase;
import com.licensis.notaire.config.GlobalExceptionHandler;

@DisplayName("ManagementController exception propagation (issue #579)")
class ManagementControllerExceptionPropagationTest {

    private final QueryWorkflowTraceUseCase workflowTrace = mock(QueryWorkflowTraceUseCase.class);
    private final MockMvc mvc = standaloneSetup(new ManagementController(
                    mock(QueryManagementsUseCase.class),
                    mock(ManageManagementsUseCase.class),
                    workflowTrace,
                    mock(TransitionManagementUseCase.class),
                    mock(TransitionManagementWebMapper.class),
                    new CompleteCaseWebMapper(),
                    mock(ExternalEntityDocumentUseCase.class),
                    mock(DocumentReentryUseCase.class)))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    @DisplayName("unexpected workflow-trace failure returns 500 ErrorResponse via GlobalExceptionHandler (#579)")
    void unexpectedWorkflowTraceFailureUsesGlobalHandler() throws Exception {
        when(workflowTrace.buildTrace(anyInt())).thenThrow(new RuntimeException("Database error"));

        mvc.perform(get("/api/v1/managements/1/workflow-trace"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
