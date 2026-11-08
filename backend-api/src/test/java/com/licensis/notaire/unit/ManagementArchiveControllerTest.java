package com.licensis.notaire.unit;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.licensis.notaire.adapter.in.web.management.ManagementArchiveExceptionHandler;
import com.licensis.notaire.adapter.in.web.management.CompleteCaseWebMapper;
import com.licensis.notaire.adapter.in.web.management.ManagementController;
import com.licensis.notaire.adapter.in.web.management.TransitionManagementWebMapper;
import com.licensis.notaire.application.port.in.document.ExternalEntityDocumentUseCase;
import com.licensis.notaire.application.port.in.management.ManageManagementsUseCase;
import com.licensis.notaire.application.port.in.management.QueryManagementsUseCase;
import com.licensis.notaire.application.port.in.management.TransitionManagementUseCase;
import com.licensis.notaire.application.port.in.workflow.DocumentReentryUseCase;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowTraceUseCase;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.ArchiveResult;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.ArchivedManagementView;
import com.licensis.notaire.config.GlobalExceptionHandler;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU16"})
@ExtendWith(MockitoExtension.class)
@DisplayName("ManagementController archive")
class ManagementArchiveControllerTest {

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
                .setControllerAdvice(new ManagementArchiveExceptionHandler(), new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /archive returns the archived management")
    void shouldArchiveManagement() throws Exception {
        when(manageManagements.archive(1, false)).thenReturn(new ArchiveResult.Ok(
                new ArchivedManagementView(1, new BigDecimal("5000.00"), true)));

        mockMvc.perform(post("/api/v1/managements/1/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idManagement").value(1))
                .andExpect(jsonPath("$.pendingBalance").value(5000.00))
                .andExpect(jsonPath("$.pendingDebtAtArchiving").value(true));
    }

    @Test
    @DisplayName("POST /archive forwards confirmado to the port")
    void shouldForwardConfirmation() throws Exception {
        when(manageManagements.archive(1, true)).thenReturn(new ArchiveResult.Ok(
                new ArchivedManagementView(1, new BigDecimal("0.00"), false)));

        mockMvc.perform(post("/api/v1/managements/1/archive").param("confirmado", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingDebtAtArchiving").value(false));
    }

    @Test
    @DisplayName("POST /archive returns 404 with an empty body when the management does not exist")
    void shouldReturnNotFoundWhenManagementMissing() throws Exception {
        when(manageManagements.archive(999, false)).thenReturn(new ArchiveResult.NotFound());

        mockMvc.perform(post("/api/v1/managements/999/archive"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("POST /archive returns 409 with waiting folder numbers")
    void shouldRejectArchiveWhenFoldersAreWaiting() throws Exception {
        when(manageManagements.archive(1, false)).thenReturn(new ArchiveResult.WaitingFolders(
                "The management has procedure folders on hold that are unresolved; "
                        + "confirm explicitly to archive anyway",
                List.of(12, 14)));

        mockMvc.perform(post("/api/v1/managements/1/archive"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "The management has procedure folders on hold that are unresolved; "
                                + "confirm explicitly to archive anyway"))
                .andExpect(jsonPath("$.waitingFolderNumbers[0]").value(12))
                .andExpect(jsonPath("$.waitingFolderNumbers[1]").value(14));
    }
}
