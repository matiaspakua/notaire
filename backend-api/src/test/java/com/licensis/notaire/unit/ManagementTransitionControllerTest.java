package com.licensis.notaire.unit;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.licensis.notaire.adapter.in.web.management.ManagementController;
import com.licensis.notaire.adapter.in.web.management.TransitionManagementWebMapper;
import com.licensis.notaire.application.port.in.document.ExternalEntityDocumentUseCase;
import com.licensis.notaire.application.port.in.management.ManageManagementsUseCase;
import com.licensis.notaire.application.port.in.management.QueryManagementsUseCase;
import com.licensis.notaire.application.port.in.management.TransitionManagementOutput;
import com.licensis.notaire.application.port.in.management.TransitionManagementUseCase;
import com.licensis.notaire.application.port.in.workflow.DocumentReentryUseCase;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowTraceUseCase;
import com.licensis.notaire.config.GlobalExceptionHandler;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU83"})
@ExtendWith(MockitoExtension.class)
@DisplayName("ManagementController transition")
class ManagementTransitionControllerTest {

    private static final String BODY = "{\"statusDestination\":\"In progress\"}";

    @Mock
    private QueryManagementsUseCase queryManagements;

    @Mock
    private ManageManagementsUseCase manageManagements;

    @Mock
    private QueryWorkflowTraceUseCase workflowTrace;

    @Mock
    private TransitionManagementUseCase transitionManagementUseCase;

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
                        new TransitionManagementWebMapper(queryManagements),
                        externalEntityDocuments,
                        documentReentry))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /managements/{id}/transition returns 200 with id and current status")
    void shouldReturnTypedTransitionBody() throws Exception {
        when(transitionManagementUseCase.execute(7, "In progress"))
                .thenReturn(new TransitionManagementOutput(7));
        when(queryManagements.findCurrentStatusName(7)).thenReturn(Optional.of("In progress"));

        mockMvc.perform(post("/api/v1/managements/7/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.currentStatus").value("In progress"));
    }

    @Test
    @DisplayName("POST /managements/{id}/transition returns 400 with message when the workflow rejects it")
    void shouldReturnBadRequestMessage() throws Exception {
        when(transitionManagementUseCase.execute(7, "In progress"))
                .thenThrow(new BusinessValidationException("Transition not allowed"));

        mockMvc.perform(post("/api/v1/managements/7/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Transition not allowed"));
    }

    @Test
    @DisplayName("POST /managements/{id}/transition returns 404 with message when the management is missing")
    void shouldReturnNotFoundMessage() throws Exception {
        when(transitionManagementUseCase.execute(999, "In progress"))
                .thenThrow(new ResourceNotFoundException("Management not found with ID: 999"));

        mockMvc.perform(post("/api/v1/managements/999/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Management not found with ID: 999"));
    }
}
