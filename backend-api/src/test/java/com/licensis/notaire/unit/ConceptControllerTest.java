package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.ConceptController;
import com.licensis.notaire.dto.DtoConcept;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.repository.ConceptRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ConceptoController unit tests")
@ExtendWith(MockitoExtension.class)
class ConceptControllerTest {

    @Mock
    private ConceptRepository repository;

    @Mock
    private com.licensis.notaire.repository.BudgetTemplateRepository templateRepository;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        ConceptController controller = new ConceptController(repository, templateRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();
    }

    private Concept buildEntity() {
        Concept c = new Concept();
        c.setIdConcept(1);
        c.setName("Honorarios");
        c.setValue(100.0f);
        c.setPercentage(0);
        c.setEnabled(true);
        return c;
    }

    @Test
    @DisplayName("GET /api/v1/conceptos should return 200 with mapped DTOs")
    void shouldReturnAllConceptos() throws Exception {
        when(repository.findAll()).thenReturn(List.of(buildEntity()));
        mockMvc.perform(get("/api/v1/conceptos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idConcept").value(1))
                .andExpect(jsonPath("$[0].name").value("Honorarios"));
    }

    @Test
    @DisplayName("GET /api/v1/conceptos/{id} should return 200 when found")
    void shouldReturnConceptByIdWhenFound() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(buildEntity()));
        mockMvc.perform(get("/api/v1/conceptos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idConcept").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/conceptos/{id} should return 404 when not found")
    void shouldReturn404WhenConceptMissing() throws Exception {
        when(repository.findById(999)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/conceptos/999"))
                .andExpect(status().isNotFound());
    }

    private DtoConcept buildDto() {
        DtoConcept dto = new DtoConcept();
        dto.setName("Honorarios");
        dto.setValue(100f);
        dto.setPercentage(0);
        dto.setEnabled(true);
        dto.setVersion(0);
        dto.setFixed(false);
        return dto;
    }

    @Test
    @DisplayName("POST /api/v1/conceptos should return 201 when created")
    void shouldCreateConcept() throws Exception {
        when(repository.save(any(Concept.class))).thenReturn(buildEntity());
        mockMvc.perform(post("/api/v1/conceptos")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/conceptos should return 409 when save fails")
    void shouldReturn409WhenCreateFails() throws Exception {
        when(repository.save(any(Concept.class))).thenThrow(new RuntimeException("boom"));
        mockMvc.perform(post("/api/v1/conceptos")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/conceptos/{id} should return 200 when update succeeds")
    void shouldUpdateConcept() throws Exception {
        Concept existing = buildEntity();
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(any(Concept.class))).thenReturn(existing);

        mockMvc.perform(put("/api/v1/conceptos/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/conceptos/{id} should return 200 when payload omits habilitado")
    void shouldUpdateConceptWhenEnabledOmitted() throws Exception {
        // Regression: a payload without 'habilitado' must not NPE while unboxing
        // a null Boolean — the controller preserves the existing enabled state.
        Concept existing = buildEntity();
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(any(Concept.class))).thenReturn(existing);

        DtoConcept partial = buildDto();
        partial.setEnabled(null);

        mockMvc.perform(put("/api/v1/conceptos/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(partial)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/conceptos/{id} should return 404 when not found")
    void shouldReturn404WhenUpdatingMissingConcept() throws Exception {
        when(repository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/v1/conceptos/99")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/conceptos/{id} should return 500 when save fails")
    void shouldReturn500WhenUpdateSaveFails() throws Exception {
        Concept existing = buildEntity();
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(any(Concept.class))).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(put("/api/v1/conceptos/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("DELETE /api/v1/conceptos/{id} should return 204 when deleted")
    void shouldDeleteConcept() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        mockMvc.perform(delete("/api/v1/conceptos/1"))
                .andExpect(status().isNoContent());
        verify(repository).deleteById(1);
    }

    @Test
    @DisplayName("DELETE /api/v1/conceptos/{id} should return 404 when missing")
    void shouldReturn404WhenDeletingMissingConcept() throws Exception {
        when(repository.existsById(99)).thenReturn(false);
        mockMvc.perform(delete("/api/v1/conceptos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/conceptos/{id} should return 409 when concepto is in use")
    void shouldReturn409WhenConceptIsInUse() throws Exception {
        com.licensis.notaire.business.BudgetTemplate pp = new com.licensis.notaire.business.BudgetTemplate();
        when(repository.existsById(1)).thenReturn(true);
        when(templateRepository.findByConceptIdConcept(1)).thenReturn(List.of(pp));
        mockMvc.perform(delete("/api/v1/conceptos/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }
}
