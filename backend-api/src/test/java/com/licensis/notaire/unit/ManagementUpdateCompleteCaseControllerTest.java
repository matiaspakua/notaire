package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.licensis.notaire.adapter.in.web.management.CompleteCaseWebMapper;
import com.licensis.notaire.adapter.in.web.management.ManagementController;
import com.licensis.notaire.adapter.in.web.management.TransitionManagementWebMapper;
import com.licensis.notaire.application.port.in.document.ExternalEntityDocumentUseCase;
import com.licensis.notaire.application.port.in.management.ManageManagementsUseCase;
import com.licensis.notaire.application.port.in.management.QueryManagementsUseCase;
import com.licensis.notaire.application.port.in.management.TransitionManagementUseCase;
import com.licensis.notaire.application.port.in.workflow.DocumentReentryUseCase;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowTraceUseCase;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.CompleteCaseResult;
import com.licensis.notaire.dto.DtoManagementSummary;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU02"})
@ExtendWith(MockitoExtension.class)
@DisplayName("ManagementController update complete-case")
class ManagementUpdateCompleteCaseControllerTest {

    private static final String BODY =
            """
            {"number":42,"heading":"Deed","budgetId":1,"notaryId":2,"statusManagementId":3,"typeProcedureId":4}
            """;

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
                .build();
    }

    @Test
    @DisplayName("PUT /managements/{id}/complete-case returns 200 with the updated management summary")
    void shouldUpdateCompleteCase() throws Exception {
        var summary = new DtoManagementSummary(7, 42, "Deed", new Date(), "Draft", 1, "note");
        when(manageManagements.updateCompleteCase(eq(7), any()))
                .thenReturn(new CompleteCaseResult.Updated(summary));

        mockMvc.perform(put("/api/v1/managements/7/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idManagement").value(7))
                .andExpect(jsonPath("$.number").value(42))
                .andExpect(jsonPath("$.heading").value("Deed"));
    }

    @Test
    @DisplayName("PUT /managements/{id}/complete-case returns 400 with an empty body")
    void shouldReturnEmptyBadRequest() throws Exception {
        when(manageManagements.updateCompleteCase(eq(7), any())).thenReturn(new CompleteCaseResult.BadRequest());

        mockMvc.perform(put("/api/v1/managements/7/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("PUT /managements/{id}/complete-case returns 404 with an empty body")
    void shouldReturnEmptyNotFound() throws Exception {
        when(manageManagements.updateCompleteCase(eq(7), any())).thenReturn(new CompleteCaseResult.NotFound());

        mockMvc.perform(put("/api/v1/managements/7/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("PUT /managements/{id}/complete-case returns 201 with an empty body when the port reports Created")
    void shouldReturnEmptyCreatedWhenCreated() throws Exception {
        when(manageManagements.updateCompleteCase(eq(7), any()))
                .thenReturn(new CompleteCaseResult.Created(new DtoManagementSummary(1, 1, "x", null, null, 0, null)));

        mockMvc.perform(put("/api/v1/managements/7/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }
}
