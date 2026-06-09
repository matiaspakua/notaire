package com.licensis.notaire.unit;

import com.licensis.notaire.api.WorkflowValidationController;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import com.licensis.notaire.service.WorkflowValidationService;
import com.licensis.notaire.service.WorkflowValidationService.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CU72 - WorkflowValidationController unit tests")
@ExtendWith(MockitoExtension.class)
class WorkflowValidationControllerTest {

    @Mock
    private WorkflowDefinitionRepository workflowRepository;
    @Mock
    private WorkflowNodeRepository nodeRepository;
    @Mock
    private WorkflowTransitionRepository transitionRepository;
    @Mock
    private WorkflowValidationService validationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        WorkflowValidationController controller = new WorkflowValidationController(
                workflowRepository, nodeRepository, transitionRepository, validationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("POST /api/v1/workflow-definition/{id}/validate should return 200 when valid")
    void shouldReturnValidWhenWorkflowIsConsistent() throws Exception {
        when(workflowRepository.existsById(1)).thenReturn(true);
        when(nodeRepository.findByWorkflowDefinitionId(1)).thenReturn(List.of());
        when(transitionRepository.findByWorkflowDefinitionId(1)).thenReturn(List.of());
        when(validationService.validate(any(), any())).thenReturn(ValidationResult.valid());

        mockMvc.perform(post("/api/v1/workflow-definition/1/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/workflow-definition/{id}/validate should return 200 with errors when invalid")
    void shouldReturnErrorsWhenWorkflowIsInconsistent() throws Exception {
        when(workflowRepository.existsById(1)).thenReturn(true);
        when(nodeRepository.findByWorkflowDefinitionId(1)).thenReturn(List.of());
        when(transitionRepository.findByWorkflowDefinitionId(1)).thenReturn(List.of());
        when(validationService.validate(any(), any())).thenReturn(
                ValidationResult.invalid(List.of("Falta nodo inicial", "Falta nodo final")));

        mockMvc.perform(post("/api/v1/workflow-definition/1/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]").value("Falta nodo inicial"));
    }

    @Test
    @DisplayName("POST /api/v1/workflow-definition/{id}/validate should return 404 when workflow missing")
    void shouldReturn404WhenWorkflowMissing() throws Exception {
        when(workflowRepository.existsById(99)).thenReturn(false);
        mockMvc.perform(post("/api/v1/workflow-definition/99/validate"))
                .andExpect(status().isNotFound());
    }
}
