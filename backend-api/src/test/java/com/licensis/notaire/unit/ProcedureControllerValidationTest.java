package com.licensis.notaire.unit;

import com.licensis.notaire.adapter.in.web.procedure.ProcedureController;
import com.licensis.notaire.application.port.in.procedure.ManageProceduresUseCase;
import com.licensis.notaire.application.port.in.procedure.QueryProceduresUseCase;
import com.licensis.notaire.config.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ProcedureController bean validation (#655)")
@ExtendWith(MockitoExtension.class)
class ProcedureControllerValidationTest {

    @Mock
    private QueryProceduresUseCase queryProcedures;

    @Mock
    private ManageProceduresUseCase manageProcedures;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProcedureController(queryProcedures, manageProcedures))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/procedures returns 400 for empty body")
    void shouldRejectEmptyCreateBody() throws Exception {
        mockMvc.perform(post("/api/v1/procedures")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/procedures/{id} returns 400 for empty body")
    void shouldRejectEmptyUpdateBody() throws Exception {
        mockMvc.perform(put("/api/v1/procedures/1")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
