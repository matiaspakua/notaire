package com.licensis.notaire.adapter.in.web.management;

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

import com.licensis.notaire.application.port.in.management.ManageManagementStatusesUseCase;
import com.licensis.notaire.application.port.in.management.QueryManagementStatusesUseCase;
import com.licensis.notaire.config.GlobalExceptionHandler;

@DisplayName("ManagementStatusController exception propagation (issue #579)")
class ManagementStatusControllerExceptionPropagationTest {

    private final QueryManagementStatusesUseCase query = mock(QueryManagementStatusesUseCase.class);
    private final ManageManagementStatusesUseCase manage = mock(ManageManagementStatusesUseCase.class);
    private final MockMvc mvc = standaloneSetup(new ManagementStatusController(query, manage))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static final String BODY = "{\"name\": \"En trámite\", \"notes\": \"x\", \"version\": 0}";

    @Test
    @DisplayName("unexpected create failure returns 500 ErrorResponse via GlobalExceptionHandler (#579)")
    void unexpectedCreateFailureUsesGlobalHandler() throws Exception {
        when(manage.create(any())).thenThrow(new RuntimeException("Database error"));

        mvc.perform(post("/api/v1/management-statuses").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
