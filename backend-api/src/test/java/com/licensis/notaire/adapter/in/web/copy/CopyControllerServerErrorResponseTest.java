package com.licensis.notaire.adapter.in.web.copy;

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

import com.licensis.notaire.application.port.in.copy.ManageCopiesUseCase;
import com.licensis.notaire.application.port.in.copy.QueryCopiesUseCase;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort.SaveResult;
import com.licensis.notaire.config.GlobalExceptionHandler;

@DisplayName("CopyController ServerError ErrorResponse (issue #579)")
class CopyControllerServerErrorResponseTest {

    private final ManageCopiesUseCase manage = mock(ManageCopiesUseCase.class);
    private final MockMvc mvc = standaloneSetup(new CopyController(mock(QueryCopiesUseCase.class), manage))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static final String BODY = "{\"number\":1,\"datePrinting\":\"2024-01-15T00:00:00.000+00:00\"}";

    @Test
    @DisplayName("create ServerError returns 500 ErrorResponse body (#579)")
    void createServerErrorReturnsErrorResponseBody() throws Exception {
        when(manage.create(any())).thenReturn(new SaveResult.ServerError());

        mvc.perform(post("/api/v1/copies").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
