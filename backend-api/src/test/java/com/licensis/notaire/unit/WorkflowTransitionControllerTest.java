package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.adapter.in.web.workflow.WorkflowTransitionController;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowTransitionsUseCase;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowTransitionsUseCase.CreateWorkflowTransitionCommand;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowTransitionsUseCase;
import com.licensis.notaire.domain.workflow.WorkflowTransition;
import com.licensis.notaire.domain.workflow.WorkflowTransitionInvalidException;
import com.licensis.notaire.domain.workflow.WorkflowTransitionNotFoundException;
import com.licensis.notaire.dto.DtoWorkflowTransition;

@DisplayName("CU83 - WorkflowTransitionController unit tests")
@ExtendWith(MockitoExtension.class)
class WorkflowTransitionControllerTest {

    @Mock
    private QueryWorkflowTransitionsUseCase query;

    @Mock
    private ManageWorkflowTransitionsUseCase manage;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkflowTransitionController(query, manage)).build();
        mapper = new ObjectMapper();
    }

    private WorkflowTransition domain() {
        return WorkflowTransition.restore(1, 1, 1, 2, null, "Start → Review", 0);
    }

    private DtoWorkflowTransition buildDto() {
        DtoWorkflowTransition dto = new DtoWorkflowTransition();
        dto.setWorkflowDefinitionId(1);
        dto.setOriginNodeId(1);
        dto.setDestinationNodeId(2);
        dto.setDescription("Start → Review");
        dto.setVersion(0);
        return dto;
    }

    @Test
    @DisplayName("GET /api/v1/workflow-transition/by-workflow/{id} should return transitions")
    void shouldReturnTransitionsForWorkflow() throws Exception {
        when(query.findByWorkflowDefinitionId(1)).thenReturn(List.of(domain()));
        mockMvc.perform(get("/api/v1/workflow-transition/by-workflow/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Start → Review"));
    }

    @Test
    @DisplayName("GET /api/v1/workflow-transition/{id} should return 200 when found")
    void shouldReturnTransitionById() throws Exception {
        when(query.findById(1)).thenReturn(Optional.of(domain()));
        mockMvc.perform(get("/api/v1/workflow-transition/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/workflow-transition/{id} should return 404 when missing")
    void shouldReturn404WhenTransitionMissing() throws Exception {
        when(query.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/workflow-transition/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/workflow-transition should return 201 when created")
    void shouldCreateTransition() throws Exception {
        when(manage.create(any(CreateWorkflowTransitionCommand.class))).thenReturn(domain());
        mockMvc.perform(post("/api/v1/workflow-transition")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/workflow-transition should return 404 when origin node missing")
    void shouldReturn404WhenOriginNodeMissing() throws Exception {
        when(manage.create(any(CreateWorkflowTransitionCommand.class)))
                .thenThrow(WorkflowTransitionInvalidException.missingOriginNode());
        mockMvc.perform(post("/api/v1/workflow-transition")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-transition/{id} should return 200 when deleted")
    void shouldDeleteTransition() throws Exception {
        doNothing().when(manage).delete(eq(1));
        mockMvc.perform(delete("/api/v1/workflow-transition/1")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-transition/{id} should return 404 when missing")
    void shouldReturn404WhenDeletingMissingTransition() throws Exception {
        doThrow(new WorkflowTransitionNotFoundException(99)).when(manage).delete(anyInt());
        mockMvc.perform(delete("/api/v1/workflow-transition/99")).andExpect(status().isNotFound());
    }
}
