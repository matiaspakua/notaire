package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.WorkflowTransitionController;
import com.licensis.notaire.dto.DtoWorkflowTransition;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowNodeType;
import com.licensis.notaire.negocio.WorkflowTransition;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CU83 - WorkflowTransitionController unit tests")
@ExtendWith(MockitoExtension.class)
class WorkflowTransitionControllerTest {

    @Mock
    private WorkflowTransitionRepository repository;
    @Mock
    private WorkflowNodeRepository nodeRepository;
    @Mock
    private WorkflowDefinitionRepository workflowRepository;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        WorkflowTransitionController controller = new WorkflowTransitionController(
                repository, nodeRepository, workflowRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();
    }

    private WorkflowNode buildNode(int id, WorkflowNodeType tipo) {
        WorkflowNode node = new WorkflowNode(id);
        node.setTipo(tipo);
        node.setWorkflowDefinition(new WorkflowDefinition(1));
        return node;
    }

    private WorkflowTransition buildEntity() {
        WorkflowTransition t = new WorkflowTransition();
        t.setId(1);
        t.setWorkflowDefinition(new WorkflowDefinition(1));
        t.setNodoOrigen(buildNode(1, WorkflowNodeType.INITIAL));
        t.setNodoDestino(buildNode(2, WorkflowNodeType.INTERMEDIATE));
        t.setDescripcion("Inicio → Revisión");
        return t;
    }

    private DtoWorkflowTransition buildDto() {
        DtoWorkflowTransition dto = new DtoWorkflowTransition();
        dto.setWorkflowDefinitionId(1);
        dto.setNodoOrigenId(1);
        dto.setNodoDestinoId(2);
        dto.setDescripcion("Inicio → Revisión");
        dto.setVersion(0);
        return dto;
    }

    @Test
    @DisplayName("GET /api/v1/workflow-transition/by-workflow/{id} should return transitions")
    void shouldReturnTransitionsForWorkflow() throws Exception {
        when(repository.findByWorkflowDefinitionId(1)).thenReturn(List.of(buildEntity()));
        mockMvc.perform(get("/api/v1/workflow-transition/by-workflow/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Inicio → Revisión"));
    }

    @Test
    @DisplayName("GET /api/v1/workflow-transition/{id} should return 200 when found")
    void shouldReturnTransitionById() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(buildEntity()));
        mockMvc.perform(get("/api/v1/workflow-transition/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/workflow-transition/{id} should return 404 when missing")
    void shouldReturn404WhenTransitionMissing() throws Exception {
        when(repository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/workflow-transition/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/workflow-transition should return 201 when created")
    void shouldCreateTransition() throws Exception {
        WorkflowTransition saved = buildEntity();
        WorkflowDefinition wf = new WorkflowDefinition(1);
        when(workflowRepository.findById(1)).thenReturn(Optional.of(wf));
        when(nodeRepository.findById(1)).thenReturn(Optional.of(buildNode(1, WorkflowNodeType.INITIAL)));
        when(nodeRepository.findById(2)).thenReturn(Optional.of(buildNode(2, WorkflowNodeType.INTERMEDIATE)));
        when(repository.save(any(WorkflowTransition.class))).thenReturn(saved);
        mockMvc.perform(post("/api/v1/workflow-transition")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/workflow-transition should return 404 when origin node missing")
    void shouldReturn404WhenOriginNodeMissing() throws Exception {
        when(workflowRepository.findById(1)).thenReturn(Optional.of(new WorkflowDefinition(1)));
        when(nodeRepository.findById(1)).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/v1/workflow-transition")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-transition/{id} should return 200 when deleted")
    void shouldDeleteTransition() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        mockMvc.perform(delete("/api/v1/workflow-transition/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/workflow-transition/{id} should return 404 when missing")
    void shouldReturn404WhenDeletingMissingTransition() throws Exception {
        when(repository.existsById(99)).thenReturn(false);
        mockMvc.perform(delete("/api/v1/workflow-transition/99"))
                .andExpect(status().isNotFound());
    }
}
