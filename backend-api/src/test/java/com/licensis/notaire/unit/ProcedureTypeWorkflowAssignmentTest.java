package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.adapter.in.web.procedure.ProcedureTypeController;
import com.licensis.notaire.application.port.in.procedure.ManageProcedureTypesUseCase;
import com.licensis.notaire.application.port.in.procedure.QueryProcedureTypesUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.WorkflowAssignResult;
import com.licensis.notaire.dto.DtoProcedureType;

@DisplayName("CU83 - ProcedureType workflow assignment tests")
@ExtendWith(MockitoExtension.class)
class ProcedureTypeWorkflowAssignmentTest {

    @Mock
    private QueryProcedureTypesUseCase query;
    @Mock
    private ManageProcedureTypesUseCase manage;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProcedureTypeController(query, manage)).build();
        mapper = new ObjectMapper();
    }

    private DtoProcedureType buildDto() {
        DtoProcedureType dto = new DtoProcedureType();
        dto.setIdProcedureType(1);
        dto.setName("Compraventa");
        dto.setEnabled(true);
        return dto;
    }

    @Test
    @DisplayName("PUT /api/v1/procedure-types/{id}/workflow should assign active workflow")
    void shouldAssignActiveWorkflow() throws Exception {
        when(manage.assignWorkflow(eq(1), eq(5))).thenReturn(new WorkflowAssignResult.Ok(buildDto()));

        mockMvc.perform(put("/api/v1/procedure-types/1/workflow")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("workflowDefinitionId", 5))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/procedure-types/{id}/workflow should return 409 for inactive workflow")
    void shouldReturn409WhenWorkflowInactive() throws Exception {
        when(manage.assignWorkflow(eq(1), eq(5))).thenReturn(new WorkflowAssignResult.Inactive());

        mockMvc.perform(put("/api/v1/procedure-types/1/workflow")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("workflowDefinitionId", 5))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/procedure-types/{id}/workflow should return 404 when the procedure type is not found")
    void shouldReturn404WhenTypeNotFound() throws Exception {
        when(manage.assignWorkflow(eq(99), eq(5))).thenReturn(new WorkflowAssignResult.NotFound());
        mockMvc.perform(put("/api/v1/procedure-types/99/workflow")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("workflowDefinitionId", 5))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/procedure-types/{id}/workflow should unassign workflow when workflowDefinitionId is null")
    void shouldUnassignWorkflowWhenIdNull() throws Exception {
        when(manage.assignWorkflow(eq(1), any())).thenReturn(new WorkflowAssignResult.Ok(buildDto()));

        mockMvc.perform(put("/api/v1/procedure-types/1/workflow")
                        .contentType("application/json")
                        .content("{\"workflowDefinitionId\": null}"))
                .andExpect(status().isOk());
    }
}
