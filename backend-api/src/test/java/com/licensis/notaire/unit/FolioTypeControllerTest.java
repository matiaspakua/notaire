package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.FolioTypeController;
import com.licensis.notaire.dto.DtoFolioType;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.FolioTypeRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("TipoDeFolioController unit tests — CU68 Buscar tipos de folio + integridad referencial")
@ExtendWith(MockitoExtension.class)
class FolioTypeControllerTest {

    @Mock
    private FolioTypeRepository repository;

    @Mock
    private FolioRepository folioRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        FolioTypeController controller = new FolioTypeController(repository, folioRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private FolioType buildEntity() {
        FolioType t = new FolioType();
        t.setIdFolioType(1);
        t.setName("Protocolo");
        return t;
    }

    @Test
    @DisplayName("CU68 — GET /search?name= should return matching tipos de folio")
    void shouldSearchByName() throws Exception {
        when(repository.findByNameContaining("Proto")).thenReturn(List.of(buildEntity()));
        mockMvc.perform(get("/api/v1/tipo-folio/search").param("name", "Proto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Protocolo"));
    }

    @Test
    @DisplayName("GET /{id}/in-use should return false when no folio references the type")
    void shouldReportNotInUse() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        when(folioRepository.findByFkIdFolioTypeIdFolioType(1)).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/tipo-folio/1/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("GET /{id}/in-use should return true when at least one folio references the type")
    void shouldReportInUse() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        when(folioRepository.findByFkIdFolioTypeIdFolioType(1)).thenReturn(List.of(new Folio()));
        mockMvc.perform(get("/api/v1/tipo-folio/1/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(true));
    }

    @Test
    @DisplayName("GET /{id}/in-use should return 404 when type de folio does not exist")
    void shouldReturn404WhenCheckingInUseForMissingType() throws Exception {
        when(repository.existsById(99)).thenReturn(false);
        mockMvc.perform(get("/api/v1/tipo-folio/99/in-use"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /{id} should return 409 when type de folio is in use")
    void shouldReturn409WhenUpdatingInUseType() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(buildEntity()));
        when(folioRepository.findByFkIdFolioTypeIdFolioType(1)).thenReturn(List.of(new Folio()));

        DtoFolioType dto = new DtoFolioType();
        dto.setName("Protocolo Modificado");

        mockMvc.perform(put("/api/v1/tipo-folio/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /{id} should succeed when type de folio is not in use")
    void shouldUpdateWhenNotInUse() throws Exception {
        FolioType existing = buildEntity();
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(folioRepository.findByFkIdFolioTypeIdFolioType(1)).thenReturn(List.of());
        when(repository.save(any(FolioType.class))).thenReturn(existing);

        DtoFolioType dto = new DtoFolioType();
        dto.setName("Protocolo Modificado");

        mockMvc.perform(put("/api/v1/tipo-folio/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /{id} should return 409 when type de folio is in use")
    void shouldReturn409WhenDeletingInUseType() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        when(folioRepository.findByFkIdFolioTypeIdFolioType(1)).thenReturn(List.of(new Folio()));

        mockMvc.perform(delete("/api/v1/tipo-folio/1"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /{id} should succeed when type de folio is not in use")
    void shouldDeleteWhenNotInUse() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        when(folioRepository.findByFkIdFolioTypeIdFolioType(1)).thenReturn(List.of());

        mockMvc.perform(delete("/api/v1/tipo-folio/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CU81 — PUT /{id} should mark a type de folio as Protocolo Auxiliar")
    void shouldMarkFolioTypeAsAuxiliary() throws Exception {
        FolioType existing = buildEntity();
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(folioRepository.findByFkIdFolioTypeIdFolioType(1)).thenReturn(List.of());
        when(repository.save(any(FolioType.class))).thenReturn(existing);

        DtoFolioType dto = new DtoFolioType();
        dto.setName("Protocolo Auxiliar");
        dto.setIsAuxiliary(true);

        mockMvc.perform(put("/api/v1/tipo-folio/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(existing.isIsAuxiliary()).isTrue();
    }
}
