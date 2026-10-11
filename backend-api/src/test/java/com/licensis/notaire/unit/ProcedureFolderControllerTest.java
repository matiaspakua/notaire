package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.adapter.in.web.procedure.ProcedureFolderController;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.application.usecase.procedure.ProcedureFolderService;
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
@DisplayName("ProcedureFolderController tests")
class ProcedureFolderControllerTest {

    @Mock
    private ProcedureFolderService procedureFolderService;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProcedureFolderController(procedureFolderService)).build();
        mapper = new ObjectMapper();
    }

    private ProcedureFolder buildFolder(Integer idFolder, int number, String status) {
        DeedManagement management = new DeedManagement();
        management.setIdManagement(1);
        Procedure procedure = new Procedure();
        procedure.setIdProcedure(10);
        ProcedureFolder folder = new ProcedureFolder();
        folder.setIdFolder(idFolder);
        folder.setNumber(number);
        folder.setStatus(status);
        folder.setFkIdManagement(management);
        folder.setFkIdProcedure(procedure);
        return folder;
    }

    @Test
    @DisplayName("GET /api/v1/procedure-folders/{id} returns the folder when found")
    void shouldReturnFolderById() throws Exception {
        when(procedureFolderService.findById(1)).thenReturn(Optional.of(buildFolder(1, 1, "Activa")));

        mockMvc.perform(get("/api/v1/procedure-folders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Activa"));
    }

    @Test
    @DisplayName("GET /api/v1/procedure-folders/{id} returns 404 when not found")
    void shouldReturnNotFoundWhenFolderMissing() throws Exception {
        when(procedureFolderService.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/procedure-folders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/procedure-folders?procedureId= returns the matching folder")
    void shouldSearchByProcedureId() throws Exception {
        when(procedureFolderService.findByProcedure(10)).thenReturn(Optional.of(buildFolder(1, 1, "Activa")));

        mockMvc.perform(get("/api/v1/procedure-folders").param("procedureId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idProcedure").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/procedure-folders?managementId= returns all folders of a management")
    void shouldSearchByManagementId() throws Exception {
        when(procedureFolderService.findByManagement(1)).thenReturn(List.of(buildFolder(1, 1, "Activa")));

        mockMvc.perform(get("/api/v1/procedure-folders").param("managementId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idManagement").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/procedure-folders without filters returns empty list")
    void shouldReturnEmptyListWithoutFilters() throws Exception {
        mockMvc.perform(get("/api/v1/procedure-folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("PUT /api/v1/procedure-folders/{id}/wait puts the folder on hold when a reason is provided")
    void shouldPutFolderOnHold() throws Exception {
        when(procedureFolderService.putOnHold(1, "Missing documentation"))
                .thenReturn(buildFolder(1, 1, "Espera"));

        mockMvc.perform(put("/api/v1/procedure-folders/1/wait")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new ProcedureFolderController.WaitRequest("Missing documentation"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Espera"));
    }

    @Test
    @DisplayName("PUT /api/v1/procedure-folders/{id}/wait returns 400 when the reason is missing")
    void shouldRejectWaitWithoutReason() throws Exception {
        when(procedureFolderService.putOnHold(1, null))
                .thenThrow(new BusinessValidationException("The reason is required to put a folder on hold"));

        mockMvc.perform(put("/api/v1/procedure-folders/1/wait")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ProcedureFolderController.WaitRequest(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The reason is required to put a folder on hold"));
    }

    @Test
    @DisplayName("PUT /api/v1/procedure-folders/{id}/wait returns 404 when the folder does not exist")
    void shouldReturnNotFoundWhenPuttingWaitOnMissingFolder() throws Exception {
        when(procedureFolderService.putOnHold(999, "Reason"))
                .thenThrow(new ResourceNotFoundException("Folder not found with ID: 999"));

        mockMvc.perform(put("/api/v1/procedure-folders/999/wait")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ProcedureFolderController.WaitRequest("Reason"))))
                .andExpect(status().isNotFound());
    }
}
