package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.TipoDeTramiteController;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.PlantillaPresupuestoRepository;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.repository.PlantillaTramiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CU83 - TipoDeTramite workflow assignment tests")
@ExtendWith(MockitoExtension.class)
class TipoDeTramiteWorkflowAssignmentTest {

    @Mock
    private TipoDeTramiteRepository repository;
    @Mock
    private WorkflowDefinitionRepository workflowRepository;
    @Mock
    private PlantillaPresupuestoRepository plantillaPresupuestoRepository;
    @Mock
    private TramiteRepository tramiteRepository;
    @Mock
    private PlantillaTramiteRepository plantillaTramiteRepository;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        TipoDeTramiteController controller = new TipoDeTramiteController(
                repository, plantillaPresupuestoRepository, tramiteRepository,
                plantillaTramiteRepository, workflowRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();
    }

    private TipoDeTramite buildTipo() {
        TipoDeTramite t = new TipoDeTramite(1);
        t.setNombre("Compraventa");
        t.setHabilitado(true);
        return t;
    }

    @Test
    @DisplayName("PUT /api/v1/tipo-tramite/{id}/workflow should assign active workflow")
    void shouldAssignActiveWorkflow() throws Exception {
        TipoDeTramite tipo = buildTipo();
        WorkflowDefinition wf = new WorkflowDefinition(5);
        wf.setNombre("Workflow Compraventa");
        wf.setActivo(true);

        when(repository.findById(1)).thenReturn(Optional.of(tipo));
        when(workflowRepository.findById(5)).thenReturn(Optional.of(wf));
        when(repository.save(any(TipoDeTramite.class))).thenReturn(tipo);

        mockMvc.perform(put("/api/v1/tipo-tramite/1/workflow")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("workflowDefinitionId", 5))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/tipo-tramite/{id}/workflow should return 409 for inactive workflow")
    void shouldReturn409WhenWorkflowInactive() throws Exception {
        TipoDeTramite tipo = buildTipo();
        WorkflowDefinition wf = new WorkflowDefinition(5);
        wf.setNombre("Workflow Inactivo");
        wf.setActivo(false);

        when(repository.findById(1)).thenReturn(Optional.of(tipo));
        when(workflowRepository.findById(5)).thenReturn(Optional.of(wf));

        mockMvc.perform(put("/api/v1/tipo-tramite/1/workflow")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("workflowDefinitionId", 5))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/tipo-tramite/{id}/workflow should return 404 when tipo not found")
    void shouldReturn404WhenTipoNotFound() throws Exception {
        when(repository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/v1/tipo-tramite/99/workflow")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("workflowDefinitionId", 5))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/tipo-tramite/{id}/workflow should unassign workflow when workflowDefinitionId is null")
    void shouldUnassignWorkflowWhenIdNull() throws Exception {
        TipoDeTramite tipo = buildTipo();
        when(repository.findById(1)).thenReturn(Optional.of(tipo));
        when(repository.save(any(TipoDeTramite.class))).thenReturn(tipo);

        mockMvc.perform(put("/api/v1/tipo-tramite/1/workflow")
                        .contentType("application/json")
                        .content("{\"workflowDefinitionId\": null}"))
                .andExpect(status().isOk());
    }
}
