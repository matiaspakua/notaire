package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.licensis.notaire.adapter.in.web.management.CompleteCaseWebMapper;
import com.licensis.notaire.adapter.in.web.management.ManagementController;
import com.licensis.notaire.adapter.in.web.management.ManagementWorkflowTraceExceptionHandler;
import com.licensis.notaire.adapter.in.web.management.TransitionManagementWebMapper;
import com.licensis.notaire.application.port.in.document.ExternalEntityDocumentUseCase;
import com.licensis.notaire.application.port.in.management.ManageManagementsUseCase;
import com.licensis.notaire.application.port.in.management.QueryManagementsUseCase;
import com.licensis.notaire.application.port.in.management.TransitionManagementUseCase;
import com.licensis.notaire.application.port.in.workflow.DocumentReentryUseCase;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowTraceUseCase;
import com.licensis.notaire.dto.DtoManagementWorkflowTrace;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagementController workflow trace")
class ManagementWorkflowTraceControllerTest {

    @Mock
    private QueryManagementsUseCase queryManagements;

    @Mock
    private ManageManagementsUseCase manageManagements;

    @Mock
    private QueryWorkflowTraceUseCase workflowTrace;

    @Mock
    private TransitionManagementUseCase transitionManagementUseCase;

    @Mock
    private TransitionManagementWebMapper transitionManagementWebMapper;

    @Mock
    private ExternalEntityDocumentUseCase externalEntityDocuments;

    @Mock
    private DocumentReentryUseCase documentReentry;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ManagementController(
                        queryManagements,
                        manageManagements,
                        workflowTrace,
                        transitionManagementUseCase,
                        transitionManagementWebMapper,
                        new CompleteCaseWebMapper(),
                        externalEntityDocuments,
                        documentReentry))
                .setControllerAdvice(new ManagementWorkflowTraceExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /managements/{id}/workflow-trace returns 200 with the trace body")
    void shouldReturnWorkflowTrace() throws Exception {
        var trace = new DtoManagementWorkflowTrace();
        trace.setManagementId(1);
        trace.setCurrentStatus("Draft");
        when(workflowTrace.buildTrace(anyInt())).thenReturn(trace);

        mockMvc.perform(get("/api/v1/managements/1/workflow-trace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managementId").value(1))
                .andExpect(jsonPath("$.currentStatus").value("Draft"));
    }

    @Test
    @DisplayName("GET /managements/{id}/workflow-trace returns 400 with an error message")
    void shouldReturnBadRequestWithErrorBody() throws Exception {
        when(workflowTrace.buildTrace(anyInt())).thenThrow(new IllegalArgumentException("Management not found"));

        mockMvc.perform(get("/api/v1/managements/999/workflow-trace"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Management not found"));
    }
}
