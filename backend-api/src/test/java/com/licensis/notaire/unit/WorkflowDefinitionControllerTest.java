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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.licensis.notaire.adapter.in.web.workflow.WorkflowDefinitionController;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowDefinitionsUseCase;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowDefinitionsUseCase.SaveWorkflowDefinitionCommand;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowDefinitionsUseCase;
import com.licensis.notaire.domain.workflow.WorkflowDefinition;
import com.licensis.notaire.domain.workflow.WorkflowDefinitionInUseException;
import com.licensis.notaire.domain.workflow.WorkflowDefinitionNotFoundException;
import com.licensis.notaire.dto.DtoWorkflowDefinition;

@DisplayName("CU83 - WorkflowDefinitionController unit tests")
@ExtendWith(MockitoExtension.class)
class WorkflowDefinitionControllerTest {

    @Mock
    private QueryWorkflowDefinitionsUseCase query;

    @Mock
    private ManageWorkflowDefinitionsUseCase manage;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkflowDefinitionController(query, manage)).build();
        mapper = new ObjectMapper();
    }

    private WorkflowDefinition domain() {
        return WorkflowDefinition.restore(1, "Workflow Compraventa", "Standard workflow", false, 0);
    }

    private DtoWorkflowDefinition buildDto() {
        DtoWorkflowDefinition dto = new DtoWorkflowDefinition();
        dto.setName("Workflow Compraventa");
        dto.setDescription("Standard workflow");
        dto.setActive(false);
        dto.setVersion(0);
        return dto;
    }

    @Test
    @DisplayName("GET /api/v1/workflow-definition should return 200 with list")
    void shouldReturnAllWorkflows() throws Exception {
        when(query.findAll()).thenReturn(List.of(domain()));
        mockMvc.perform(get("/api/v1/workflow-definition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Workflow Compraventa"));
    }

    @Test
    @DisplayName("GET /api/v1/workflow-definition/{id} should return 200 when found")
    void shouldReturnWorkflowById() throws Exception {
        when(query.findById(1)).thenReturn(Optional.of(domain()));
        mockMvc.perform(get("/api/v1/workflow-definition/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Workflow Compraventa"));
    }

    @Test
    @DisplayName("GET /api/v1/workflow-definition/{id} should return 404 when missing")
    void shouldReturn404WhenWorkflowMissing() throws Exception {
        when(query.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/workflow-definition/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/workflow-definition should return 201 when created")
    void shouldCreateWorkflow() throws Exception {
        when(manage.create(any(SaveWorkflowDefinitionCommand.class))).thenReturn(domain());
        mockMvc.perform(post("/api/v1/workflow-definition")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Workflow Compraventa"));
    }

    @Test
    @DisplayName("POST /api/v1/workflow-definition should return 409 when save fails")
    void shouldReturn409WhenCreateFails() throws Exception {
        when(manage.create(any(SaveWorkflowDefinitionCommand.class))).thenThrow(new RuntimeException("conflict"));
        mockMvc.perform(post("/api/v1/workflow-definition")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/workflow-definition/{id} should return 200 when updated")
    void shouldUpdateWorkflow() throws Exception {
        doNothing().when(manage).update(eq(1), any(SaveWorkflowDefinitionCommand.class));
        mockMvc.perform(put("/api/v1/workflow-definition/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/workflow-definition/{id} should return 404 when missing")
    void shouldReturn404WhenUpdatingMissingWorkflow() throws Exception {
        doThrow(new WorkflowDefinitionNotFoundException(99))
                .when(manage)
                .update(eq(99), any(SaveWorkflowDefinitionCommand.class));
        mockMvc.perform(put("/api/v1/workflow-definition/99")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-definition/{id} should return 204 when deleted")
    void shouldDeleteWorkflow() throws Exception {
        doNothing().when(manage).delete(1);
        mockMvc.perform(delete("/api/v1/workflow-definition/1")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-definition/{id} should return 404 when missing")
    void shouldReturn404WhenDeletingMissingWorkflow() throws Exception {
        doThrow(new WorkflowDefinitionNotFoundException(99)).when(manage).delete(99);
        mockMvc.perform(delete("/api/v1/workflow-definition/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-definition/{id} should return 409 when nodes exist")
    void shouldReturn409WhenDeletingWorkflowWithNodes() throws Exception {
        doThrow(WorkflowDefinitionInUseException.cannotDelete()).when(manage).delete(anyInt());
        mockMvc.perform(delete("/api/v1/workflow-definition/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(WorkflowDefinitionInUseException.DELETE_MESSAGE));
    }
}
