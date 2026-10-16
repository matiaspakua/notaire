package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.adapter.in.web.procedure.ProcedureTemplateController;
import com.licensis.notaire.application.port.in.procedure.ManageProcedureTemplatesUseCase;
import com.licensis.notaire.application.port.in.procedure.QueryProcedureTemplatesUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort.SaveResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort.TemplateView;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcedureTemplateController unit tests")
class ProcedureTemplateControllerTest {

    @Mock
    private QueryProcedureTemplatesUseCase queryProcedureTemplates;

    @Mock
    private ManageProcedureTemplatesUseCase manageProcedureTemplates;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ProcedureTemplateController(queryProcedureTemplates, manageProcedureTemplates))
                .build();
        mapper = new ObjectMapper();
    }

    private TemplateView view(int procedureTypeId, int documentTypeId) {
        return new TemplateView(procedureTypeId, documentTypeId, "Observation test", 0);
    }

    @Test
    @DisplayName("GET /procedure-templates returns list")
    void shouldReturnAll() throws Exception {
        when(queryProcedureTemplates.findAll()).thenReturn(List.of(view(1, 2)));
        mockMvc.perform(get("/api/v1/procedure-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /procedure-templates/procedure-type/{id} returns list")
    void shouldReturnByTypeProcedure() throws Exception {
        when(queryProcedureTemplates.findByProcedureType(1)).thenReturn(List.of(view(1, 2)));
        mockMvc.perform(get("/api/v1/procedure-templates/procedure-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /procedure-templates/{idProcedureType}/{idDocumentType} returns 200 when found")
    void shouldReturnByCompositeId() throws Exception {
        when(queryProcedureTemplates.findById(1, 2)).thenReturn(Optional.of(view(1, 2)));
        mockMvc.perform(get("/api/v1/procedure-templates/1/2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /procedure-templates/{idProcedureType}/{idDocumentType} returns 404 when not found")
    void shouldReturn404WhenNotFound() throws Exception {
        when(queryProcedureTemplates.findById(9, 9)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/procedure-templates/9/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /procedure-templates creates new entry and returns 201")
    void shouldCreateTemplate() throws Exception {
        when(manageProcedureTemplates.create(eq(1), eq(2), any())).thenReturn(new SaveResult.Created(view(1, 2)));
        mockMvc.perform(post("/api/v1/procedure-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fkIdProcedureType":1,"fkIdDocumentType":2,"notes":"Observation test"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /procedure-templates/{idProcedureType}/{idDocumentType} returns 200 when exists")
    void shouldUpdateTemplate() throws Exception {
        when(manageProcedureTemplates.update(eq(1), eq(2), any())).thenReturn(new SaveResult.Updated(view(1, 2)));
        mockMvc.perform(put("/api/v1/procedure-templates/1/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"notes":"Observation test"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /procedure-templates/{idProcedureType}/{idDocumentType} returns 404 when it does not exist")
    void shouldReturn404OnUpdate() throws Exception {
        when(manageProcedureTemplates.update(eq(9), eq(9), any())).thenReturn(new SaveResult.NotFound());
        mockMvc.perform(put("/api/v1/procedure-templates/9/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"notes":"x"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /procedure-templates/{idProcedureType}/{idDocumentType} returns 204 when exists")
    void shouldDeleteTemplate() throws Exception {
        when(manageProcedureTemplates.delete(1, 2)).thenReturn(new DeleteResult.Deleted());
        mockMvc.perform(delete("/api/v1/procedure-templates/1/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /procedure-templates/{idProcedureType}/{idDocumentType} returns 404 when it does not exist")
    void shouldReturn404OnDelete() throws Exception {
        when(manageProcedureTemplates.delete(9, 9)).thenReturn(new DeleteResult.NotFound());
        mockMvc.perform(delete("/api/v1/procedure-templates/9/9"))
                .andExpect(status().isNotFound());
    }
}
