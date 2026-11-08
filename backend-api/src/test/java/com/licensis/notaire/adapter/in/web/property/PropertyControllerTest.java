package com.licensis.notaire.adapter.in.web.property;

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

import com.licensis.notaire.application.port.in.property.ManagePropertiesUseCase;
import com.licensis.notaire.application.port.in.property.QueryPropertiesUseCase;
import com.licensis.notaire.config.GlobalExceptionHandler;

@DisplayName("PropertyController exception propagation (issue #579)")
class PropertyControllerTest {

    private final QueryPropertiesUseCase query = mock(QueryPropertiesUseCase.class);
    private final ManagePropertiesUseCase manage = mock(ManagePropertiesUseCase.class);
    private final MockMvc mvc = standaloneSetup(new PropertyController(query, manage))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static final String BODY = "{\"cadastralDesignation\": \"PADRON-1\"}";

    @Test
    @DisplayName("unexpected create failure returns 500 ErrorResponse via GlobalExceptionHandler (#579)")
    void unexpectedCreateFailureUsesGlobalHandler() throws Exception {
        when(manage.create(any())).thenThrow(new RuntimeException("Database error"));

        mvc.perform(post("/api/v1/properties").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
