package com.licensis.notaire.adapter.in.web.folio;

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

import com.licensis.notaire.application.port.in.folio.ManageFoliosUseCase;
import com.licensis.notaire.application.port.in.folio.QueryFoliosUseCase;
import com.licensis.notaire.config.GlobalExceptionHandler;

@DisplayName("FolioController exception propagation (issue #579)")
class FolioControllerExceptionPropagationTest {

    private final QueryFoliosUseCase query = mock(QueryFoliosUseCase.class);
    private final ManageFoliosUseCase manage = mock(ManageFoliosUseCase.class);
    private final MockMvc mvc = standaloneSetup(new FolioController(query, manage))
            .setControllerAdvice(new FolioExceptionHandler(), new GlobalExceptionHandler())
            .build();

    private static final String BODY =
            "{\"number\": 1, \"year\": 2026, \"status\": \"Disponible\", \"typeFolioId\": 1, \"notaryId\": 1}";

    @Test
    @DisplayName("unexpected create failure returns 500 ErrorResponse via GlobalExceptionHandler (#579)")
    void unexpectedCreateFailureUsesGlobalHandler() throws Exception {
        when(manage.create(any())).thenThrow(new RuntimeException("Database error"));

        mvc.perform(post("/api/v1/folios").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
