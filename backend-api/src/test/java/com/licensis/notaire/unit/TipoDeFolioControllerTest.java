package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.TipoDeFolioController;
import com.licensis.notaire.dto.DtoTipoDeFolio;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.TipoDeFolio;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.TipoDeFolioRepository;
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
class TipoDeFolioControllerTest {

    @Mock
    private TipoDeFolioRepository repository;

    @Mock
    private FolioRepository folioRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        TipoDeFolioController controller = new TipoDeFolioController(repository, folioRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private TipoDeFolio buildEntity() {
        TipoDeFolio t = new TipoDeFolio();
        t.setIdTipoFolio(1);
        t.setNombre("Protocolo");
        return t;
    }

    @Test
    @DisplayName("CU68 — GET /search?nombre= should return matching tipos de folio")
    void shouldSearchByNombre() throws Exception {
        when(repository.findByNombreContaining("Proto")).thenReturn(List.of(buildEntity()));
        mockMvc.perform(get("/api/v1/tipo-folio/search").param("nombre", "Proto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Protocolo"));
    }

    @Test
    @DisplayName("GET /{id}/in-use should return false when no folio references the tipo")
    void shouldReportNotInUse() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        when(folioRepository.findByFkIdTipoFolioIdTipoFolio(1)).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/tipo-folio/1/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("GET /{id}/in-use should return true when at least one folio references the tipo")
    void shouldReportInUse() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        when(folioRepository.findByFkIdTipoFolioIdTipoFolio(1)).thenReturn(List.of(new Folio()));
        mockMvc.perform(get("/api/v1/tipo-folio/1/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(true));
    }

    @Test
    @DisplayName("GET /{id}/in-use should return 404 when tipo de folio does not exist")
    void shouldReturn404WhenCheckingInUseForMissingTipo() throws Exception {
        when(repository.existsById(99)).thenReturn(false);
        mockMvc.perform(get("/api/v1/tipo-folio/99/in-use"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /{id} should return 409 when tipo de folio is in use")
    void shouldReturn409WhenUpdatingInUseTipo() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(buildEntity()));
        when(folioRepository.findByFkIdTipoFolioIdTipoFolio(1)).thenReturn(List.of(new Folio()));

        DtoTipoDeFolio dto = new DtoTipoDeFolio();
        dto.setNombre("Protocolo Modificado");

        mockMvc.perform(put("/api/v1/tipo-folio/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /{id} should succeed when tipo de folio is not in use")
    void shouldUpdateWhenNotInUse() throws Exception {
        TipoDeFolio existing = buildEntity();
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(folioRepository.findByFkIdTipoFolioIdTipoFolio(1)).thenReturn(List.of());
        when(repository.save(any(TipoDeFolio.class))).thenReturn(existing);

        DtoTipoDeFolio dto = new DtoTipoDeFolio();
        dto.setNombre("Protocolo Modificado");

        mockMvc.perform(put("/api/v1/tipo-folio/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /{id} should return 409 when tipo de folio is in use")
    void shouldReturn409WhenDeletingInUseTipo() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        when(folioRepository.findByFkIdTipoFolioIdTipoFolio(1)).thenReturn(List.of(new Folio()));

        mockMvc.perform(delete("/api/v1/tipo-folio/1"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /{id} should succeed when tipo de folio is not in use")
    void shouldDeleteWhenNotInUse() throws Exception {
        when(repository.existsById(1)).thenReturn(true);
        when(folioRepository.findByFkIdTipoFolioIdTipoFolio(1)).thenReturn(List.of());

        mockMvc.perform(delete("/api/v1/tipo-folio/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CU81 — PUT /{id} should mark a tipo de folio as Protocolo Auxiliar")
    void shouldMarkTipoDeFolioAsAuxiliar() throws Exception {
        TipoDeFolio existing = buildEntity();
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(folioRepository.findByFkIdTipoFolioIdTipoFolio(1)).thenReturn(List.of());
        when(repository.save(any(TipoDeFolio.class))).thenReturn(existing);

        DtoTipoDeFolio dto = new DtoTipoDeFolio();
        dto.setNombre("Protocolo Auxiliar");
        dto.setEsAuxiliar(true);

        mockMvc.perform(put("/api/v1/tipo-folio/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(existing.isEsAuxiliar()).isTrue();
    }
}
