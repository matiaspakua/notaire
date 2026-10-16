package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.adapter.in.web.registrationdraft.RegistrationDraftController;
import com.licensis.notaire.application.port.in.registrationdraft.ManageRegistrationDraftsUseCase;
import com.licensis.notaire.application.port.in.registrationdraft.QueryRegistrationDraftsUseCase;
import com.licensis.notaire.application.port.out.registrationdraft.RegistrationDraftOpsPort.DraftResult;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.dto.DtoRegistrationDraft;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequirementCoverage({"CU82"})
@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrationDraftController unit tests")
class RegistrationDraftControllerTest {

    @Mock
    private QueryRegistrationDraftsUseCase queryRegistrationDrafts;

    @Mock
    private ManageRegistrationDraftsUseCase manageRegistrationDrafts;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new RegistrationDraftController(queryRegistrationDrafts, manageRegistrationDrafts))
                .build();
        mapper = new ObjectMapper();
    }

    private DtoRegistrationDraft buildDto(Integer id, int number, String status) {
        DtoRegistrationDraft dto = new DtoRegistrationDraft();
        dto.setIdRegistrationDraft(id);
        dto.setNumber(number);
        dto.setStatus(status);
        dto.setIdDeed(1);
        return dto;
    }

    @Test
    @DisplayName("GET /api/v1/registration-drafts/{id} returns the registration draft when found")
    void shouldReturnDraftById() throws Exception {
        when(queryRegistrationDrafts.findById(1))
                .thenReturn(Optional.of(buildDto(1, 1, BusinessConstants.RegistrationDraftGenerated)));

        mockMvc.perform(get("/api/v1/registration-drafts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BusinessConstants.RegistrationDraftGenerated));
    }

    @Test
    @DisplayName("GET /api/v1/registration-drafts/{id} returns 404 when not found")
    void shouldReturnNotFoundWhenDraftMissing() throws Exception {
        when(queryRegistrationDrafts.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/registration-drafts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/registration-drafts generates a registration draft when data is complete")
    void shouldGenerateDraftWhenDataIsComplete() throws Exception {
        when(manageRegistrationDrafts.generate(1))
                .thenReturn(new DraftResult.Ok(buildDto(1, 1, BusinessConstants.RegistrationDraftGenerated)));

        mockMvc.perform(post("/api/v1/registration-drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RegistrationDraftController.GenerateRequest(1))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/registration-drafts/1"))
                .andExpect(jsonPath("$.status").value(BusinessConstants.RegistrationDraftGenerated));
    }

    @Test
    @DisplayName("POST /api/v1/registration-drafts returns 400 when data is incomplete")
    void shouldRejectGenerationWhenDataIsIncomplete() throws Exception {
        when(manageRegistrationDrafts.generate(1))
                .thenReturn(new DraftResult.Invalid(
                        "Missing cadastral/registry data for the property: registration number"));

        mockMvc.perform(post("/api/v1/registration-drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RegistrationDraftController.GenerateRequest(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Missing cadastral/registry data for the property: registration number"));
    }

    @Test
    @DisplayName("POST /api/v1/registration-drafts returns 404 when the deed does not exist")
    void shouldReturnNotFoundWhenDeedMissing() throws Exception {
        when(manageRegistrationDrafts.generate(999)).thenReturn(new DraftResult.NotFound());

        mockMvc.perform(post("/api/v1/registration-drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RegistrationDraftController.GenerateRequest(999))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/registration-drafts/{id}/submit registers the submission")
    void shouldRegisterSubmission() throws Exception {
        when(manageRegistrationDrafts.submit(any(), any(), anyString()))
                .thenReturn(new DraftResult.Ok(buildDto(1, 1, BusinessConstants.RegistrationDraftSubmitted)));

        mockMvc.perform(put("/api/v1/registration-drafts/1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new RegistrationDraftController.SubmitRequest(new Date(), "ENT-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BusinessConstants.RegistrationDraftSubmitted));
    }

    @Test
    @DisplayName("PUT /api/v1/registration-drafts/{id}/observe registers the observation")
    void shouldRegisterObservation() throws Exception {
        when(manageRegistrationDrafts.observe(anyInt(), anyString(), any()))
                .thenReturn(new DraftResult.Ok(buildDto(1, 1, BusinessConstants.RegistrationDraftObserved)));

        mockMvc.perform(put("/api/v1/registration-drafts/1/observe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new RegistrationDraftController.ObserveRequest("Missing plan", new Date()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BusinessConstants.RegistrationDraftObserved));
    }

    @Test
    @DisplayName("PUT /api/v1/registration-drafts/{id}/register registers the final registration")
    void shouldRegisterFinalRegistration() throws Exception {
        when(manageRegistrationDrafts.recordRegistration(anyInt(), any(), anyString()))
                .thenReturn(new DraftResult.Ok(buildDto(1, 1, BusinessConstants.RegistrationDraftRegistered)));

        mockMvc.perform(put("/api/v1/registration-drafts/1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new RegistrationDraftController.RegisterRequest(new Date(), "INS-456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BusinessConstants.RegistrationDraftRegistered));
    }
}
