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

import com.licensis.notaire.application.port.in.document.ExternalEntityDocumentUseCase;
import com.licensis.notaire.application.port.in.management.ManageManagementsUseCase;
import com.licensis.notaire.application.port.in.management.QueryManagementsUseCase;
import com.licensis.notaire.application.port.in.management.TransitionManagementUseCase;
import com.licensis.notaire.application.port.in.workflow.DocumentReentryUseCase;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowTraceUseCase;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.SaveResult;
import com.licensis.notaire.config.GlobalExceptionHandler;

@DisplayName("ManagementController ServerError ErrorResponse (issue #579)")
class ManagementControllerServerErrorResponseTest {

    private final ManageManagementsUseCase manage = mock(ManageManagementsUseCase.class);
    private final MockMvc mvc = standaloneSetup(new ManagementController(
                    mock(QueryManagementsUseCase.class),
                    manage,
                    mock(QueryWorkflowTraceUseCase.class),
                    mock(TransitionManagementUseCase.class),
                    mock(TransitionManagementWebMapper.class),
                    mock(ExternalEntityDocumentUseCase.class),
                    mock(DocumentReentryUseCase.class)))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static final String BODY = "{\"number\":1,\"heading\":\"Management\"}";

    @Test
    @DisplayName("create ServerError returns 500 ErrorResponse body (#579)")
    void createServerErrorReturnsErrorResponseBody() throws Exception {
        when(manage.create(any())).thenReturn(new SaveResult.ServerError());

        mvc.perform(post("/api/v1/managements").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
