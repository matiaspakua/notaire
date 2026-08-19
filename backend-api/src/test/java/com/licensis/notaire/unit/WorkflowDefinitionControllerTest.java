package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.WorkflowDefinitionController;
import com.licensis.notaire.dto.DtoWorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CU83 - WorkflowDefinitionController unit tests")
@ExtendWith(MockitoExtension.class)
class WorkflowDefinitionControllerTest {

    @Mock
    private WorkflowDefinitionRepository repository;
    @Mock
    private WorkflowNodeRepository nodeRepository;
    @Mock
    private WorkflowTransitionRepository transitionRepository;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        WorkflowDefinitionController controller = new WorkflowDefinitionController(
                repository, nodeRepository, transitionRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();
    }

    private WorkflowDefinition buildEntity() {
        WorkflowDefinition wf = new WorkflowDefinition();
        wf.setId(1);
        wf.setNombre("Workflow Compraventa");
        wf.setDescripcion("Workflow estándar");
        wf.setActivo(false);
        return wf;
    }

    private DtoWorkflowDefinition buildDto() {
        DtoWorkflowDefinition dto = new DtoWorkflowDefinition();
        dto.setNombre("Workflow Compraventa");
        dto.setDescripcion("Workflow estándar");
        dto.setActivo(false);
        dto.setVersion(0);
        return dto;
    }

    @Test
    @DisplayName("GET /api/v1/workflow-definition should return 200 with list")
    void shouldReturnAllWorkflows() throws Exception {
        when(repository.findAll()).thenReturn(List.of(buildEntity()));
        mockMvc.perform(get("/api/v1/workflow-definition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Workflow Compraventa"));
    }

    @Test
    @DisplayName("GET /api/v1/workflow-definition/{id} should return 200 when found")
    void shouldReturnWorkflowById() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(buildEntity()));
        mockMvc.perform(get("/api/v1/workflow-definition/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Workflow Compraventa"));
    }

    @Test
    @DisplayName("GET /api/v1/workflow-definition/{id} should return 404 when missing")
    void shouldReturn404WhenWorkflowMissing() throws Exception {
        when(repository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/workflow-definition/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/workflow-definition should return 201 when created")
    void shouldCreateWorkflow() throws Exception {
        WorkflowDefinition saved = buildEntity();
        when(repository.save(any(WorkflowDefinition.class))).thenReturn(saved);
        mockMvc.perform(post("/api/v1/workflow-definition")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Workflow Compraventa"));
    }

    @Test
    @DisplayName("POST /api/v1/workflow-definition should return 409 when save fails")
    void shouldReturn409WhenCreateFails() throws Exception {
        when(repository.save(any(WorkflowDefinition.class))).thenThrow(new RuntimeException("conflict"));
        mockMvc.perform(post("/api/v1/workflow-definition")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/workflow-definition/{id} should return 200 when updated")
    void shouldUpdateWorkflow() throws Exception {
        WorkflowDefinition existing = buildEntity();
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(any(WorkflowDefinition.class))).thenReturn(existing);
        mockMvc.perform(put("/api/v1/workflow-definition/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/workflow-definition/{id} should return 404 when missing")
    void shouldReturn404WhenUpdatingMissingWorkflow() throws Exception {
        when(repository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/v1/workflow-definition/99")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-definition/{id} should return 200 when deleted")
    void shouldDeleteWorkflow() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        when(nodeRepository.findByWorkflowDefinitionId(1)).thenReturn(List.of());
        mockMvc.perform(delete("/api/v1/workflow-definition/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-definition/{id} should return 404 when missing")
    void shouldReturn404WhenDeletingMissingWorkflow() throws Exception {
        when(repository.existsById(99)).thenReturn(false);
        mockMvc.perform(delete("/api/v1/workflow-definition/99"))
                .andExpect(status().isNotFound());
    }
}
