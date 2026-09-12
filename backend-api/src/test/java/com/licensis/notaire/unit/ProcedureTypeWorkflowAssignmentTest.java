package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.ProcedureTypeController;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.BudgetTemplateRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
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
class ProcedureTypeWorkflowAssignmentTest {

    @Mock
    private ProcedureTypeRepository repository;
    @Mock
    private WorkflowDefinitionRepository workflowRepository;
    @Mock
    private BudgetTemplateRepository budgetTemplateRepository;
    @Mock
    private ProcedureRepository procedureRepository;
    @Mock
    private ProcedureTemplateRepository procedureTemplateRepository;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        ProcedureTypeController controller = new ProcedureTypeController(
                repository, budgetTemplateRepository, procedureRepository,
                procedureTemplateRepository, workflowRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();
    }

    private ProcedureType buildType() {
        ProcedureType t = new ProcedureType(1);
        t.setName("Compraventa");
        t.setEnabled(true);
        return t;
    }

    @Test
    @DisplayName("PUT /api/v1/tipo-tramite/{id}/workflow should assign active workflow")
    void shouldAssignActiveWorkflow() throws Exception {
        ProcedureType type = buildType();
        WorkflowDefinition wf = new WorkflowDefinition(5);
        wf.setName("Workflow Compraventa");
        wf.setActive(true);

        when(repository.findById(1)).thenReturn(Optional.of(type));
        when(workflowRepository.findById(5)).thenReturn(Optional.of(wf));
        when(repository.save(any(ProcedureType.class))).thenReturn(type);

        mockMvc.perform(put("/api/v1/tipo-tramite/1/workflow")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("workflowDefinitionId", 5))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/tipo-tramite/{id}/workflow should return 409 for inactive workflow")
    void shouldReturn409WhenWorkflowInactive() throws Exception {
        ProcedureType type = buildType();
        WorkflowDefinition wf = new WorkflowDefinition(5);
        wf.setName("Workflow Inactivo");
        wf.setActive(false);

        when(repository.findById(1)).thenReturn(Optional.of(type));
        when(workflowRepository.findById(5)).thenReturn(Optional.of(wf));

        mockMvc.perform(put("/api/v1/tipo-tramite/1/workflow")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("workflowDefinitionId", 5))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/tipo-tramite/{id}/workflow should return 404 when tipo not found")
    void shouldReturn404WhenTypeNotFound() throws Exception {
        when(repository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/v1/tipo-tramite/99/workflow")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("workflowDefinitionId", 5))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/tipo-tramite/{id}/workflow should unassign workflow when workflowDefinitionId is null")
    void shouldUnassignWorkflowWhenIdNull() throws Exception {
        ProcedureType type = buildType();
        when(repository.findById(1)).thenReturn(Optional.of(type));
        when(repository.save(any(ProcedureType.class))).thenReturn(type);

        mockMvc.perform(put("/api/v1/tipo-tramite/1/workflow")
                        .contentType("application/json")
                        .content("{\"workflowDefinitionId\": null}"))
                .andExpect(status().isOk());
    }
}
