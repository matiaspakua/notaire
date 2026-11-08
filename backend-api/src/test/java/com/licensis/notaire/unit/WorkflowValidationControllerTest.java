package com.licensis.notaire.unit;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.licensis.notaire.adapter.in.web.workflow.WorkflowValidationController;
import com.licensis.notaire.application.port.in.workflow.ValidateWorkflowUseCase;
import com.licensis.notaire.application.port.in.workflow.ValidateWorkflowUseCase.Result;

@DisplayName("CU72 - WorkflowValidationController unit tests (issue #1770)")
@ExtendWith(MockitoExtension.class)
class WorkflowValidationControllerTest {

    @Mock
    private ValidateWorkflowUseCase validateWorkflow;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkflowValidationController(validateWorkflow))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/workflow-definition/{id}/validate should return 200 when valid")
    void shouldReturnValidWhenWorkflowIsConsistent() throws Exception {
        when(validateWorkflow.validate(1)).thenReturn(new Result.Ok(true, List.of()));

        mockMvc.perform(post("/api/v1/workflow-definition/1/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/workflow-definition/{id}/validate should return 200 with errors when invalid")
    void shouldReturnErrorsWhenWorkflowIsInconsistent() throws Exception {
        when(validateWorkflow.validate(1))
                .thenReturn(new Result.Ok(false, List.of("Missing initial node", "Missing final node")));

        mockMvc.perform(post("/api/v1/workflow-definition/1/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]").value("Missing initial node"));
    }

    @Test
    @DisplayName("POST /api/v1/workflow-definition/{id}/validate should return 404 when workflow missing")
    void shouldReturn404WhenWorkflowMissing() throws Exception {
        when(validateWorkflow.validate(99)).thenReturn(new Result.NotFound());
        mockMvc.perform(post("/api/v1/workflow-definition/99/validate"))
                .andExpect(status().isNotFound());
    }
}
