package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.PlantillaTramiteController;
import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.negocio.PlantillaTramitePK;
import com.licensis.notaire.repository.PlantillaTramiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlantillaTramiteController unit tests")
class PlantillaTramiteControllerTest {

    @Mock
    private PlantillaTramiteRepository repository;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PlantillaTramiteController(repository)).build();
        mapper = new ObjectMapper();
    }

    private PlantillaTramite buildPlantilla(int idTipoTramite, int idTipoDocumento) {
        PlantillaTramite pt = new PlantillaTramite(new PlantillaTramitePK(idTipoTramite, idTipoDocumento));
        pt.setObservaciones("Obs test");
        return pt;
    }

    @Test
    @DisplayName("GET /plantilla-tramite returns list")
    void shouldReturnAll() throws Exception {
        when(repository.findAll()).thenReturn(List.of(buildPlantilla(1, 2)));
        mockMvc.perform(get("/api/v1/plantilla-tramite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /plantilla-tramite/tipo-tramite/{id} returns list")
    void shouldReturnByTipoTramite() throws Exception {
        when(repository.findByTipoDeTramiteIdTipoTramite(1)).thenReturn(List.of(buildPlantilla(1, 2)));
        mockMvc.perform(get("/api/v1/plantilla-tramite/tipo-tramite/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /plantilla-tramite/{idTipoTramite}/{idTipoDocumento} returns 200 when found")
    void shouldReturnByCompositeId() throws Exception {
        PlantillaTramitePK pk = new PlantillaTramitePK(1, 2);
        when(repository.findById(pk)).thenReturn(Optional.of(buildPlantilla(1, 2)));
        mockMvc.perform(get("/api/v1/plantilla-tramite/1/2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /plantilla-tramite/{idTipoTramite}/{idTipoDocumento} returns 404 when not found")
    void shouldReturn404WhenNotFound() throws Exception {
        PlantillaTramitePK pk = new PlantillaTramitePK(9, 9);
        when(repository.findById(pk)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/plantilla-tramite/9/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /plantilla-tramite creates new entry and returns 201")
    void shouldCreatePlantilla() throws Exception {
        PlantillaTramite pt = buildPlantilla(1, 2);
        when(repository.save(any(PlantillaTramite.class))).thenReturn(pt);
        mockMvc.perform(post("/api/v1/plantilla-tramite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(pt)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /plantilla-tramite/{idTipoTramite}/{idTipoDocumento} returns 200 when exists")
    void shouldUpdatePlantilla() throws Exception {
        PlantillaTramitePK pk = new PlantillaTramitePK(1, 2);
        PlantillaTramite pt = buildPlantilla(1, 2);
        when(repository.existsById(pk)).thenReturn(true);
        when(repository.save(any(PlantillaTramite.class))).thenReturn(pt);
        mockMvc.perform(put("/api/v1/plantilla-tramite/1/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(pt)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /plantilla-tramite/{idTipoTramite}/{idTipoDocumento} returns 404 when not exists")
    void shouldReturn404OnUpdate() throws Exception {
        PlantillaTramitePK pk = new PlantillaTramitePK(9, 9);
        when(repository.existsById(pk)).thenReturn(false);
        mockMvc.perform(put("/api/v1/plantilla-tramite/9/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(buildPlantilla(9, 9))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /plantilla-tramite/{idTipoTramite}/{idTipoDocumento} returns 204 when exists")
    void shouldDeletePlantilla() throws Exception {
        PlantillaTramitePK pk = new PlantillaTramitePK(1, 2);
        when(repository.existsById(pk)).thenReturn(true);
        doNothing().when(repository).deleteById(pk);
        mockMvc.perform(delete("/api/v1/plantilla-tramite/1/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /plantilla-tramite/{idTipoTramite}/{idTipoDocumento} returns 404 when not exists")
    void shouldReturn404OnDelete() throws Exception {
        PlantillaTramitePK pk = new PlantillaTramitePK(9, 9);
        when(repository.existsById(pk)).thenReturn(false);
        mockMvc.perform(delete("/api/v1/plantilla-tramite/9/9"))
                .andExpect(status().isNotFound());
    }
}
