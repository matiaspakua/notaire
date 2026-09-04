package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.CarpetaTramiteController;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.CarpetaTramite;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.service.CarpetaTramiteService;
import com.licensis.notaire.testing.RequirementCoverage;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequirementCoverage({"CU85"})
@ExtendWith(MockitoExtension.class)
@DisplayName("CarpetaTramiteController unit tests")
class CarpetaTramiteControllerTest {

    @Mock
    private CarpetaTramiteService carpetaTramiteService;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CarpetaTramiteController(carpetaTramiteService)).build();
        mapper = new ObjectMapper();
    }

    private CarpetaTramite buildCarpeta(Integer idCarpeta, int numero, String estado) {
        GestionDeEscritura gestion = new GestionDeEscritura();
        gestion.setIdGestion(1);
        Tramite tramite = new Tramite();
        tramite.setIdTramite(10);
        CarpetaTramite carpeta = new CarpetaTramite();
        carpeta.setIdCarpeta(idCarpeta);
        carpeta.setNumero(numero);
        carpeta.setEstado(estado);
        carpeta.setFkIdGestion(gestion);
        carpeta.setFkIdTramite(tramite);
        return carpeta;
    }

    @Test
    @DisplayName("GET /api/v1/carpetas/{id} returns carpeta when found")
    void shouldReturnCarpetaById() throws Exception {
        when(carpetaTramiteService.findById(1)).thenReturn(Optional.of(buildCarpeta(1, 1, "Activa")));

        mockMvc.perform(get("/api/v1/carpetas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Activa"));
    }

    @Test
    @DisplayName("GET /api/v1/carpetas/{id} returns 404 when not found")
    void shouldReturnNotFoundWhenCarpetaMissing() throws Exception {
        when(carpetaTramiteService.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/carpetas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/carpetas?tramiteId= returns the matching carpeta")
    void shouldSearchByTramiteId() throws Exception {
        when(carpetaTramiteService.findByTramite(10)).thenReturn(Optional.of(buildCarpeta(1, 1, "Activa")));

        mockMvc.perform(get("/api/v1/carpetas").param("tramiteId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idTramite").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/carpetas?gestionId= returns all carpetas of a gestión")
    void shouldSearchByGestionId() throws Exception {
        when(carpetaTramiteService.findByGestion(1)).thenReturn(List.of(buildCarpeta(1, 1, "Activa")));

        mockMvc.perform(get("/api/v1/carpetas").param("gestionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idGestion").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/carpetas without filters returns empty list")
    void shouldReturnEmptyListWithoutFilters() throws Exception {
        mockMvc.perform(get("/api/v1/carpetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("PUT /api/v1/carpetas/{id}/espera puts carpeta en espera with motivo")
    void shouldPutCarpetaEnEspera() throws Exception {
        when(carpetaTramiteService.ponerEnEspera(1, "Falta documentación"))
                .thenReturn(buildCarpeta(1, 1, "Espera"));

        mockMvc.perform(put("/api/v1/carpetas/1/espera")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new CarpetaTramiteController.EsperaRequest("Falta documentación"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Espera"));
    }

    @Test
    @DisplayName("PUT /api/v1/carpetas/{id}/espera returns 400 when motivo is missing")
    void shouldRejectEsperaWithoutMotivo() throws Exception {
        when(carpetaTramiteService.ponerEnEspera(1, null))
                .thenThrow(new BusinessValidationException("El motivo es obligatorio"));

        mockMvc.perform(put("/api/v1/carpetas/1/espera")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CarpetaTramiteController.EsperaRequest(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El motivo es obligatorio"));
    }

    @Test
    @DisplayName("PUT /api/v1/carpetas/{id}/espera returns 404 when carpeta does not exist")
    void shouldReturnNotFoundWhenPuttingEsperaOnMissingCarpeta() throws Exception {
        when(carpetaTramiteService.ponerEnEspera(999, "Motivo"))
                .thenThrow(new ResourceNotFoundException("No existe la carpeta con ID: 999"));

        mockMvc.perform(put("/api/v1/carpetas/999/espera")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CarpetaTramiteController.EsperaRequest("Motivo"))))
                .andExpect(status().isNotFound());
    }
}
