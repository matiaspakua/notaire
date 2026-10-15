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
import com.licensis.notaire.adapter.in.web.workflow.WorkflowNodeController;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowNodesUseCase;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowNodesUseCase.CreateWorkflowNodeCommand;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowNodesUseCase.UpdateWorkflowNodeCommand;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowNodesUseCase;
import com.licensis.notaire.domain.workflow.WorkflowNode;
import com.licensis.notaire.domain.workflow.WorkflowNodeInUseException;
import com.licensis.notaire.domain.workflow.WorkflowNodeInvalidException;
import com.licensis.notaire.domain.workflow.WorkflowNodeKind;
import com.licensis.notaire.dto.DtoWorkflowNode;

@DisplayName("CU83 - WorkflowNodeController unit tests")
@ExtendWith(MockitoExtension.class)
class WorkflowNodeControllerTest {

    @Mock
    private QueryWorkflowNodesUseCase query;

    @Mock
    private ManageWorkflowNodesUseCase manage;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkflowNodeController(query, manage)).build();
        mapper = new ObjectMapper();
    }

    private WorkflowNode domain() {
        return WorkflowNode.restore(1, 1, 10, "Started", WorkflowNodeKind.INITIAL, 100f, 200f, 0);
    }

    private DtoWorkflowNode buildDto() {
        DtoWorkflowNode dto = new DtoWorkflowNode();
        dto.setWorkflowDefinitionId(1);
        dto.setStatusManagementId(10);
        dto.setType("INITIAL");
        dto.setPositionX(100f);
        dto.setPositionY(200f);
        dto.setVersion(0);
        return dto;
    }

    @Test
    @DisplayName("GET /api/v1/workflow-node/by-workflow/{id} should return nodes for workflow")
    void shouldReturnNodesForWorkflow() throws Exception {
        when(query.findByWorkflowDefinitionId(1)).thenReturn(List.of(domain()));
        mockMvc.perform(get("/api/v1/workflow-node/by-workflow/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("INITIAL"));
    }

    @Test
    @DisplayName("GET /api/v1/workflow-node/{id} should return 200 when found")
    void shouldReturnNodeById() throws Exception {
        when(query.findById(1)).thenReturn(Optional.of(domain()));
        mockMvc.perform(get("/api/v1/workflow-node/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("INITIAL"));
    }

    @Test
    @DisplayName("GET /api/v1/workflow-node/{id} should return 404 when missing")
    void shouldReturn404WhenNodeMissing() throws Exception {
        when(query.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/workflow-node/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/workflow-node should return 201 when created")
    void shouldCreateNode() throws Exception {
        when(manage.create(any(CreateWorkflowNodeCommand.class))).thenReturn(domain());
        mockMvc.perform(post("/api/v1/workflow-node")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/workflow-node should return 404 when workflow not found")
    void shouldReturn404WhenWorkflowMissing() throws Exception {
        when(manage.create(any(CreateWorkflowNodeCommand.class)))
                .thenThrow(WorkflowNodeInvalidException.missingWorkflow());
        mockMvc.perform(post("/api/v1/workflow-node")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/workflow-node/{id} should return 200 when updated")
    void shouldUpdateNode() throws Exception {
        doNothing().when(manage).update(eq(1), any(UpdateWorkflowNodeCommand.class));
        mockMvc.perform(put("/api/v1/workflow-node/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-node/{id} should return 204 when deleted")
    void shouldDeleteNode() throws Exception {
        doNothing().when(manage).delete(1);
        mockMvc.perform(delete("/api/v1/workflow-node/1")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-node/{id} should return 409 when node has transitions")
    void shouldReturn409WhenNodeHasTransitions() throws Exception {
        doThrow(WorkflowNodeInUseException.cannotDelete()).when(manage).delete(anyInt());
        mockMvc.perform(delete("/api/v1/workflow-node/1")).andExpect(status().isConflict());
    }
}
