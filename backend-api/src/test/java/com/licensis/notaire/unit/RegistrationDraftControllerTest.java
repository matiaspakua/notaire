package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.RegistrationDraftController;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.RegistrationDraft;
import com.licensis.notaire.service.RegistrationDraftService;
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
class RegistrationDraftControllerTest {

    @Mock
    private RegistrationDraftService registrationDraftService;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RegistrationDraftController(registrationDraftService)).build();
        mapper = new ObjectMapper();
    }

    private RegistrationDraft buildDraft(Integer id, int number, String status) {
        Deed deed = new Deed();
        deed.setIdDeed(1);
        RegistrationDraft draft = new RegistrationDraft();
        draft.setIdRegistrationDraft(id);
        draft.setNumber(number);
        draft.setStatus(status);
        draft.setFkIdDeed(deed);
        return draft;
    }

    @Test
    @DisplayName("GET /api/v1/minutas-inscripcion/{id} returns minuta when found")
    void shouldReturnDraftById() throws Exception {
        when(registrationDraftService.findById(1))
                .thenReturn(Optional.of(buildDraft(1, 1, BusinessConstants.RegistrationDraftGENERADA)));

        mockMvc.perform(get("/api/v1/minutas-inscripcion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BusinessConstants.RegistrationDraftGENERADA));
    }

    @Test
    @DisplayName("GET /api/v1/minutas-inscripcion/{id} returns 404 when not found")
    void shouldReturnNotFoundWhenDraftMissing() throws Exception {
        when(registrationDraftService.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/minutas-inscripcion/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/minutas-inscripcion generates minuta when data is complete")
    void shouldGenerateDraftWhenDataIsComplete() throws Exception {
        when(registrationDraftService.generate(1))
                .thenReturn(buildDraft(1, 1, BusinessConstants.RegistrationDraftGENERADA));

        mockMvc.perform(post("/api/v1/minutas-inscripcion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RegistrationDraftController.GenerateRequest(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BusinessConstants.RegistrationDraftGENERADA));
    }

    @Test
    @DisplayName("POST /api/v1/minutas-inscripcion returns 400 when data is incomplete")
    void shouldRejectGenerationWhenDataIsIncomplete() throws Exception {
        when(registrationDraftService.generate(1))
                .thenThrow(new BusinessValidationException(
                        "Faltan datos catastrales/registrales del property: matrícula"));

        mockMvc.perform(post("/api/v1/minutas-inscripcion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RegistrationDraftController.GenerateRequest(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Faltan datos catastrales/registrales del property: matrícula"));
    }

    @Test
    @DisplayName("POST /api/v1/minutas-inscripcion returns 404 when escritura does not exist")
    void shouldReturnNotFoundWhenDeedMissing() throws Exception {
        when(registrationDraftService.generate(999))
                .thenThrow(new ResourceNotFoundException("No existe la deed con ID: 999"));

        mockMvc.perform(post("/api/v1/minutas-inscripcion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RegistrationDraftController.GenerateRequest(999))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/minutas-inscripcion/{id}/presentar registers presentacion")
    void shouldRegisterSubmission() throws Exception {
        when(registrationDraftService.presentar(any(), any(), anyString()))
                .thenReturn(buildDraft(1, 1, BusinessConstants.RegistrationDraftPRESENTADA));

        mockMvc.perform(put("/api/v1/minutas-inscripcion/1/presentar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new RegistrationDraftController.PresentarRequest(new Date(), "ENT-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BusinessConstants.RegistrationDraftPRESENTADA));
    }

    @Test
    @DisplayName("PUT /api/v1/minutas-inscripcion/{id}/observar registers observacion")
    void shouldRegisterObservacion() throws Exception {
        when(registrationDraftService.observar(anyInt(), anyString(), any()))
                .thenReturn(buildDraft(1, 1, BusinessConstants.RegistrationDraftOBSERVADA));

        mockMvc.perform(put("/api/v1/minutas-inscripcion/1/observar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new RegistrationDraftController.ObservarRequest("Falta plano", new Date()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BusinessConstants.RegistrationDraftOBSERVADA));
    }

    @Test
    @DisplayName("PUT /api/v1/minutas-inscripcion/{id}/inscribir registers inscripcion definitiva")
    void shouldRegisterRegistrationDefinitiva() throws Exception {
        when(registrationDraftService.inscribir(anyInt(), any(), anyString()))
                .thenReturn(buildDraft(1, 1, BusinessConstants.RegistrationDraftRegistered));

        mockMvc.perform(put("/api/v1/minutas-inscripcion/1/inscribir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new RegistrationDraftController.InscribirRequest(new Date(), "INS-456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BusinessConstants.RegistrationDraftRegistered));
    }
}
