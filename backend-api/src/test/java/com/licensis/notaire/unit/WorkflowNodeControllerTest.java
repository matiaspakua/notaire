package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.WorkflowNodeController;
import com.licensis.notaire.dto.DtoWorkflowNode;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowNodeType;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
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

@DisplayName("CU83 - WorkflowNodeController unit tests")
@ExtendWith(MockitoExtension.class)
class WorkflowNodeControllerTest {

    @Mock
    private WorkflowNodeRepository repository;
    @Mock
    private WorkflowDefinitionRepository workflowRepository;
    @Mock
    private EstadoDeGestionRepository estadoRepository;
    @Mock
    private WorkflowTransitionRepository transitionRepository;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        WorkflowNodeController controller = new WorkflowNodeController(
                repository, workflowRepository, estadoRepository, transitionRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();
    }

    private WorkflowNode buildEntity() {
        WorkflowDefinition wf = new WorkflowDefinition(1);
        EstadoDeGestion estado = new EstadoDeGestion(10);
        estado.setNombre("Iniciado");
        WorkflowNode node = new WorkflowNode();
        node.setId(1);
        node.setWorkflowDefinition(wf);
        node.setEstadoDeGestion(estado);
        node.setTipo(WorkflowNodeType.INITIAL);
        node.setPosicionX(100f);
        node.setPosicionY(200f);
        return node;
    }

    private DtoWorkflowNode buildDto() {
        DtoWorkflowNode dto = new DtoWorkflowNode();
        dto.setWorkflowDefinitionId(1);
        dto.setEstadoGestionId(10);
        dto.setTipo("INITIAL");
        dto.setPosicionX(100f);
        dto.setPosicionY(200f);
        dto.setVersion(0);
        return dto;
    }

    @Test
    @DisplayName("GET /api/v1/workflow-node/by-workflow/{id} should return nodes for workflow")
    void shouldReturnNodesForWorkflow() throws Exception {
        when(repository.findByWorkflowDefinitionId(1)).thenReturn(List.of(buildEntity()));
        mockMvc.perform(get("/api/v1/workflow-node/by-workflow/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("INITIAL"));
    }

    @Test
    @DisplayName("GET /api/v1/workflow-node/{id} should return 200 when found")
    void shouldReturnNodeById() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(buildEntity()));
        mockMvc.perform(get("/api/v1/workflow-node/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("INITIAL"));
    }

    @Test
    @DisplayName("GET /api/v1/workflow-node/{id} should return 404 when missing")
    void shouldReturn404WhenNodeMissing() throws Exception {
        when(repository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/workflow-node/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/workflow-node should return 201 when created")
    void shouldCreateNode() throws Exception {
        WorkflowDefinition wf = new WorkflowDefinition(1);
        EstadoDeGestion estado = new EstadoDeGestion(10);
        WorkflowNode saved = buildEntity();
        when(workflowRepository.findById(1)).thenReturn(Optional.of(wf));
        when(estadoRepository.findById(10)).thenReturn(Optional.of(estado));
        when(repository.save(any(WorkflowNode.class))).thenReturn(saved);
        mockMvc.perform(post("/api/v1/workflow-node")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/workflow-node should return 404 when workflow not found")
    void shouldReturn404WhenWorkflowMissing() throws Exception {
        when(workflowRepository.findById(1)).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/v1/workflow-node")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/workflow-node/{id} should return 200 when updated")
    void shouldUpdateNode() throws Exception {
        WorkflowNode existing = buildEntity();
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(any(WorkflowNode.class))).thenReturn(existing);
        mockMvc.perform(put("/api/v1/workflow-node/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-node/{id} should return 200 when deleted")
    void shouldDeleteNode() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        when(transitionRepository.existsByNodoOrigenIdOrNodoDestinoId(1, 1)).thenReturn(false);
        mockMvc.perform(delete("/api/v1/workflow-node/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-node/{id} should return 409 when node has transitions")
    void shouldReturn409WhenNodeHasTransitions() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        when(transitionRepository.existsByNodoOrigenIdOrNodoDestinoId(1, 1)).thenReturn(true);
        mockMvc.perform(delete("/api/v1/workflow-node/1"))
                .andExpect(status().isConflict());
    }
}
