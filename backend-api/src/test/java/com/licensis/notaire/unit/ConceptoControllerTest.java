package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.ConceptoController;
import com.licensis.notaire.dto.DtoConcepto;
import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.repository.ConceptoRepository;
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
import static org.mockito.Mockito.doThrow;
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
class ConceptoControllerTest {

    @Mock
    private ConceptoRepository repository;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        ConceptoController controller = new ConceptoController(repository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();
    }

    private Concepto buildEntity() {
        Concepto c = new Concepto();
        c.setIdConcepto(1);
        c.setNombre("Honorarios");
        c.setValor(100.0f);
        c.setPorcentaje(0);
        c.setHabilitado(true);
        return c;
    }

    @Test
    @DisplayName("GET /api/v1/conceptos should return 200 with mapped DTOs")
    void shouldReturnAllConceptos() throws Exception {
        when(repository.findAll()).thenReturn(List.of(buildEntity()));
        mockMvc.perform(get("/api/v1/conceptos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idConcepto").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Honorarios"));
    }

    @Test
    @DisplayName("GET /api/v1/conceptos/{id} should return 200 when found")
    void shouldReturnConceptoByIdWhenFound() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(buildEntity()));
        mockMvc.perform(get("/api/v1/conceptos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idConcepto").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/conceptos/{id} should return 404 when not found")
    void shouldReturn404WhenConceptoMissing() throws Exception {
        when(repository.findById(999)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/conceptos/999"))
                .andExpect(status().isNotFound());
    }

    private DtoConcepto buildDto() {
        DtoConcepto dto = new DtoConcepto();
        dto.setNombre("Honorarios");
        dto.setValor(100f);
        dto.setPorcentaje(0);
        dto.setHabilitado(true);
        dto.setVersion(0);
        dto.setFijo(false);
        return dto;
    }

    @Test
    @DisplayName("POST /api/v1/conceptos should return 201 when created")
    void shouldCreateConcepto() throws Exception {
        when(repository.save(any(Concepto.class))).thenReturn(buildEntity());
        mockMvc.perform(post("/api/v1/conceptos")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/conceptos should return 409 when save fails")
    void shouldReturn409WhenCreateFails() throws Exception {
        when(repository.save(any(Concepto.class))).thenThrow(new RuntimeException("boom"));
        mockMvc.perform(post("/api/v1/conceptos")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/conceptos/{id} should return 200 when update succeeds")
    void shouldUpdateConcepto() throws Exception {
        Concepto existing = buildEntity();
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(any(Concepto.class))).thenReturn(existing);

        mockMvc.perform(put("/api/v1/conceptos/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/conceptos/{id} should return 404 when not found")
    void shouldReturn404WhenUpdatingMissingConcepto() throws Exception {
        when(repository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/v1/conceptos/99")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/conceptos/{id} should return 500 when save fails")
    void shouldReturn500WhenUpdateSaveFails() throws Exception {
        Concepto existing = buildEntity();
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(any(Concepto.class))).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(put("/api/v1/conceptos/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(buildDto())))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("DELETE /api/v1/conceptos/{id} should return 204 when deleted")
    void shouldDeleteConcepto() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        mockMvc.perform(delete("/api/v1/conceptos/1"))
                .andExpect(status().isNoContent());
        verify(repository).deleteById(1);
    }

    @Test
    @DisplayName("DELETE /api/v1/conceptos/{id} should return 404 when missing")
    void shouldReturn404WhenDeletingMissingConcepto() throws Exception {
        when(repository.existsById(99)).thenReturn(false);
        mockMvc.perform(delete("/api/v1/conceptos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/conceptos/{id} should return 409 when delete fails")
    void shouldReturn409WhenDeleteFails() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        doThrow(new RuntimeException("FK violation")).when(repository).deleteById(1);
        mockMvc.perform(delete("/api/v1/conceptos/1"))
                .andExpect(status().isConflict());
    }
}
