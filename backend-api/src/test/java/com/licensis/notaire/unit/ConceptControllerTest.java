package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
import com.licensis.notaire.adapter.in.web.concept.ConceptController;
import com.licensis.notaire.application.port.in.concept.ManageConceptsUseCase;
import com.licensis.notaire.application.port.in.concept.QueryConceptsUseCase;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.SaveResult;
import com.licensis.notaire.dto.DtoConcept;

@DisplayName("ConceptController unit tests")
@ExtendWith(MockitoExtension.class)
class ConceptControllerTest {

    @Mock
    private QueryConceptsUseCase queryConcepts;

    @Mock
    private ManageConceptsUseCase manageConcepts;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        ConceptController controller = new ConceptController(queryConcepts, manageConcepts);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.licensis.notaire.config.GlobalExceptionHandler())
                .build();
        mapper = new ObjectMapper();
    }

    private DtoConcept buildDto() {
        DtoConcept dto = new DtoConcept();
        dto.setIdConcept(1);
        dto.setName("Honorarios");
        dto.setValue(new java.math.BigDecimal("100"));
        dto.setPercentage(0);
        dto.setEnabled(true);
        dto.setVersion(0);
        dto.setFixed(false);
        return dto;
    }

    @Test
    @DisplayName("GET /api/v1/concepts should return 200 with mapped DTOs")
    void shouldReturnAllConcepts() throws Exception {
        when(queryConcepts.findAll()).thenReturn(List.of(buildDto()));
        mockMvc.perform(get("/api/v1/concepts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idConcept").value(1))
                .andExpect(jsonPath("$[0].name").value("Honorarios"));
    }

    @Test
    @DisplayName("GET /api/v1/concepts/{id} should return 200 when found")
    void shouldReturnConceptByIdWhenFound() throws Exception {
        when(queryConcepts.findById(1)).thenReturn(Optional.of(buildDto()));
        mockMvc.perform(get("/api/v1/concepts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idConcept").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/concepts/{id} should return 404 when not found")
    void shouldReturn404WhenConceptMissing() throws Exception {
        when(queryConcepts.findById(999)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/concepts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/concepts returns 400 for empty body (#655)")
    void shouldRejectEmptyCreateBody() throws Exception {
        mockMvc.perform(post("/api/v1/concepts")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/concepts should return 201 when created")
    void shouldCreateConcept() throws Exception {
        when(manageConcepts.create(any())).thenReturn(new SaveResult.Created(buildDto()));
        mockMvc.perform(post("/api/v1/concepts")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/concepts should return 409 when save fails")
    void shouldReturn409WhenCreateFails() throws Exception {
        when(manageConcepts.create(any()))
                .thenReturn(new SaveResult.Failed(new RuntimeException("boom"), true));
        mockMvc.perform(post("/api/v1/concepts")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/concepts/{id} should return 200 when update succeeds")
    void shouldUpdateConcept() throws Exception {
        when(manageConcepts.update(eq(1), any())).thenReturn(new SaveResult.Updated());
        mockMvc.perform(put("/api/v1/concepts/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/concepts/{id} should return 200 when payload omits enabled")
    void shouldUpdateConceptWhenEnabledOmitted() throws Exception {
        // Regression: a payload without 'enabled' must not NPE while unboxing
        // a null Boolean — the adapter preserves the existing enabled state.
        when(manageConcepts.update(eq(1), any())).thenReturn(new SaveResult.Updated());
        DtoConcept partial = buildDto();
        partial.setEnabled(null);
        mockMvc.perform(put("/api/v1/concepts/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(partial)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/concepts/{id} should return 404 when not found")
    void shouldReturn404WhenUpdatingMissingConcept() throws Exception {
        when(manageConcepts.update(eq(99), any())).thenReturn(new SaveResult.NotFound());
        mockMvc.perform(put("/api/v1/concepts/99")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/concepts/{id} should return 500 when save fails")
    void shouldReturn500WhenUpdateSaveFails() throws Exception {
        when(manageConcepts.update(eq(1), any()))
                .thenReturn(new SaveResult.Failed(new RuntimeException("boom"), false));
        mockMvc.perform(put("/api/v1/concepts/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("DELETE /api/v1/concepts/{id} should return 204 when deleted")
    void shouldDeleteConcept() throws Exception {
        when(manageConcepts.delete(1)).thenReturn(new DeleteResult.Deleted());
        mockMvc.perform(delete("/api/v1/concepts/1"))
                .andExpect(status().isNoContent());
        verify(manageConcepts).delete(1);
    }

    @Test
    @DisplayName("DELETE /api/v1/concepts/{id} should return 404 when missing")
    void shouldReturn404WhenDeletingMissingConcept() throws Exception {
        when(manageConcepts.delete(99)).thenReturn(new DeleteResult.NotFound());
        mockMvc.perform(delete("/api/v1/concepts/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/concepts/{id} should return 409 when the concept is in use")
    void shouldReturn409WhenConceptIsInUse() throws Exception {
        when(manageConcepts.delete(1))
                .thenReturn(new DeleteResult.InUse("Cannot delete: the concept is in use on budget templates."));
        mockMvc.perform(delete("/api/v1/concepts/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }
}
