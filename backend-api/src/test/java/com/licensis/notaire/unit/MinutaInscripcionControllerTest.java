package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.MinutaInscripcionController;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.MinutaInscripcion;
import com.licensis.notaire.service.MinutaInscripcionService;
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

import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequirementCoverage({"CU82"})
@ExtendWith(MockitoExtension.class)
@DisplayName("MinutaInscripcionController unit tests")
class MinutaInscripcionControllerTest {

    @Mock
    private MinutaInscripcionService minutaInscripcionService;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MinutaInscripcionController(minutaInscripcionService)).build();
        mapper = new ObjectMapper();
    }

    private MinutaInscripcion buildMinuta(Integer id, int numero, String estado) {
        Escritura escritura = new Escritura();
        escritura.setIdEscritura(1);
        MinutaInscripcion minuta = new MinutaInscripcion();
        minuta.setIdMinutaInscripcion(id);
        minuta.setNumero(numero);
        minuta.setEstado(estado);
        minuta.setFkIdEscritura(escritura);
        return minuta;
    }

    @Test
    @DisplayName("GET /api/v1/minutas-inscripcion/{id} returns minuta when found")
    void shouldReturnMinutaById() throws Exception {
        when(minutaInscripcionService.findById(1))
                .thenReturn(Optional.of(buildMinuta(1, 1, ConstantesNegocio.MINUTA_INSCRIPCION_GENERADA)));

        mockMvc.perform(get("/api/v1/minutas-inscripcion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(ConstantesNegocio.MINUTA_INSCRIPCION_GENERADA));
    }

    @Test
    @DisplayName("GET /api/v1/minutas-inscripcion/{id} returns 404 when not found")
    void shouldReturnNotFoundWhenMinutaMissing() throws Exception {
        when(minutaInscripcionService.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/minutas-inscripcion/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/minutas-inscripcion generates minuta when data is complete")
    void shouldGenerateMinutaWhenDataIsComplete() throws Exception {
        when(minutaInscripcionService.generar(1))
                .thenReturn(buildMinuta(1, 1, ConstantesNegocio.MINUTA_INSCRIPCION_GENERADA));

        mockMvc.perform(post("/api/v1/minutas-inscripcion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new MinutaInscripcionController.GenerarRequest(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(ConstantesNegocio.MINUTA_INSCRIPCION_GENERADA));
    }

    @Test
    @DisplayName("POST /api/v1/minutas-inscripcion returns 400 when data is incomplete")
    void shouldRejectGenerationWhenDataIsIncomplete() throws Exception {
        when(minutaInscripcionService.generar(1))
                .thenThrow(new BusinessValidationException(
                        "Faltan datos catastrales/registrales del inmueble: matrícula"));

        mockMvc.perform(post("/api/v1/minutas-inscripcion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new MinutaInscripcionController.GenerarRequest(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Faltan datos catastrales/registrales del inmueble: matrícula"));
    }

    @Test
    @DisplayName("POST /api/v1/minutas-inscripcion returns 404 when escritura does not exist")
    void shouldReturnNotFoundWhenEscrituraMissing() throws Exception {
        when(minutaInscripcionService.generar(999))
                .thenThrow(new ResourceNotFoundException("No existe la escritura con ID: 999"));

        mockMvc.perform(post("/api/v1/minutas-inscripcion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new MinutaInscripcionController.GenerarRequest(999))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/minutas-inscripcion/{id}/presentar registers presentacion")
    void shouldRegisterPresentacion() throws Exception {
        when(minutaInscripcionService.presentar(any(), any(), anyString()))
                .thenReturn(buildMinuta(1, 1, ConstantesNegocio.MINUTA_INSCRIPCION_PRESENTADA));

        mockMvc.perform(put("/api/v1/minutas-inscripcion/1/presentar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new MinutaInscripcionController.PresentarRequest(new Date(), "ENT-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(ConstantesNegocio.MINUTA_INSCRIPCION_PRESENTADA));
    }

    @Test
    @DisplayName("PUT /api/v1/minutas-inscripcion/{id}/observar registers observacion")
    void shouldRegisterObservacion() throws Exception {
        when(minutaInscripcionService.observar(anyInt(), anyString(), any()))
                .thenReturn(buildMinuta(1, 1, ConstantesNegocio.MINUTA_INSCRIPCION_OBSERVADA));

        mockMvc.perform(put("/api/v1/minutas-inscripcion/1/observar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new MinutaInscripcionController.ObservarRequest("Falta plano", new Date()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(ConstantesNegocio.MINUTA_INSCRIPCION_OBSERVADA));
    }

    @Test
    @DisplayName("PUT /api/v1/minutas-inscripcion/{id}/inscribir registers inscripcion definitiva")
    void shouldRegisterInscripcionDefinitiva() throws Exception {
        when(minutaInscripcionService.inscribir(anyInt(), any(), anyString()))
                .thenReturn(buildMinuta(1, 1, ConstantesNegocio.MINUTA_INSCRIPCION_INSCRIPTA));

        mockMvc.perform(put("/api/v1/minutas-inscripcion/1/inscribir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new MinutaInscripcionController.InscribirRequest(new Date(), "INS-456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(ConstantesNegocio.MINUTA_INSCRIPCION_INSCRIPTA));
    }
}
