package com.licensis.notaire.adapter.in.web.procedure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.licensis.notaire.application.port.in.procedure.ManageProceduresUseCase;
import com.licensis.notaire.application.port.in.procedure.QueryProceduresUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.SaveResult;
import com.licensis.notaire.config.GlobalExceptionHandler;

@DisplayName("ProcedureController ServerError ErrorResponse (issue #579)")
class ProcedureControllerServerErrorResponseTest {

    private final ManageProceduresUseCase manage = mock(ManageProceduresUseCase.class);
    private final MockMvc mvc = standaloneSetup(new ProcedureController(mock(QueryProceduresUseCase.class), manage))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static final String BODY = "{\"idProcedureType\":1,\"notes\":\"x\"}";

    @Test
    @DisplayName("create ServerError returns 500 ErrorResponse body (#579)")
    void createServerErrorReturnsErrorResponseBody() throws Exception {
        when(manage.create(any())).thenReturn(new SaveResult.ServerError());

        mvc.perform(post("/api/v1/procedures").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
