package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.licensis.notaire.application.port.in.management.TransitionManagementUseCase;
import com.licensis.notaire.application.port.in.workflow.DocumentReentryUseCase;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowTraceUseCase;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.ManagementView;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.SaveResult;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU02"})
@ExtendWith(MockitoExtension.class)
@DisplayName("ManagementController create")
class ManagementCreateControllerTest {

    private static final String BODY = "{\"number\":42,\"heading\":\"Deed\"}";

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
                        externalEntityDocuments,
                        documentReentry))
                .build();
    }

    @Test
    @DisplayName("POST /managements returns 201 with the created management")
    void shouldCreateManagement() throws Exception {
        when(manageManagements.create(any())).thenReturn(new SaveResult.Created(
                new ManagementView(7, 42, "Deed", null, "note", 3, 4, false, 1)));

        mockMvc.perform(post("/api/v1/managements").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idManagement").value(7))
                .andExpect(jsonPath("$.number").value(42))
                .andExpect(jsonPath("$.heading").value("Deed"))
                .andExpect(jsonPath("$.notes").value("note"))
                .andExpect(jsonPath("$.notaryPersonId").value(3))
                .andExpect(jsonPath("$.managementStatusId").value(4))
                .andExpect(jsonPath("$.pendingDebtAtArchiving").value(false))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    @DisplayName("POST /managements returns 400 with an empty body")
    void shouldReturnEmptyBadRequest() throws Exception {
        when(manageManagements.create(any())).thenReturn(new SaveResult.BadRequest());

        mockMvc.perform(post("/api/v1/managements").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("POST /managements returns 404 with an empty body")
    void shouldReturnEmptyNotFound() throws Exception {
        when(manageManagements.create(any())).thenReturn(new SaveResult.NotFound());

        mockMvc.perform(post("/api/v1/managements").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("POST /managements returns 200 with an empty body when the port reports Updated")
    void shouldReturnEmptyOkWhenUpdated() throws Exception {
        when(manageManagements.create(any())).thenReturn(new SaveResult.Updated());

        mockMvc.perform(post("/api/v1/managements").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
