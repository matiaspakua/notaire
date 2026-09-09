package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.ProcedureFolderController;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.service.ProcedureFolderService;
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
    @DisplayName("GET /api/v1/carpetas/{id} returns carpeta when found")
    void shouldReturnFolderById() throws Exception {
        when(procedureFolderService.findById(1)).thenReturn(Optional.of(buildFolder(1, 1, "Activa")));

        mockMvc.perform(get("/api/v1/carpetas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Activa"));
    }

    @Test
    @DisplayName("GET /api/v1/carpetas/{id} returns 404 when not found")
    void shouldReturnNotFoundWhenFolderMissing() throws Exception {
        when(procedureFolderService.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/carpetas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/carpetas?tramiteId= returns the matching carpeta")
    void shouldSearchByProcedureId() throws Exception {
        when(procedureFolderService.findByProcedure(10)).thenReturn(Optional.of(buildFolder(1, 1, "Activa")));

        mockMvc.perform(get("/api/v1/carpetas").param("procedureId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idProcedure").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/carpetas?gestionId= returns all carpetas of a gestión")
    void shouldSearchByManagementId() throws Exception {
        when(procedureFolderService.findByManagement(1)).thenReturn(List.of(buildFolder(1, 1, "Activa")));

        mockMvc.perform(get("/api/v1/carpetas").param("managementId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idManagement").value(1));
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
    void shouldPutFolderEnWait() throws Exception {
        when(procedureFolderService.ponerEnWait(1, "Falta documentación"))
                .thenReturn(buildFolder(1, 1, "Espera"));

        mockMvc.perform(put("/api/v1/carpetas/1/espera")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new ProcedureFolderController.WaitRequest("Falta documentación"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Espera"));
    }

    @Test
    @DisplayName("PUT /api/v1/carpetas/{id}/espera returns 400 when motivo is missing")
    void shouldRejectWaitWithoutReason() throws Exception {
        when(procedureFolderService.ponerEnWait(1, null))
                .thenThrow(new BusinessValidationException("El motivo es obligatorio"));

        mockMvc.perform(put("/api/v1/carpetas/1/espera")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ProcedureFolderController.WaitRequest(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El motivo es obligatorio"));
    }

    @Test
    @DisplayName("PUT /api/v1/carpetas/{id}/espera returns 404 when carpeta does not exist")
    void shouldReturnNotFoundWhenPuttingWaitOnMissingFolder() throws Exception {
        when(procedureFolderService.ponerEnWait(999, "Motivo"))
                .thenThrow(new ResourceNotFoundException("No existe la carpeta con ID: 999"));

        mockMvc.perform(put("/api/v1/carpetas/999/espera")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ProcedureFolderController.WaitRequest("Motivo"))))
                .andExpect(status().isNotFound());
    }
}
