package com.licensis.notaire.adapter.in.web.history;

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

import com.licensis.notaire.application.port.in.history.ManageHistoriesUseCase;
import com.licensis.notaire.application.port.in.history.QueryHistoriesUseCase;
import com.licensis.notaire.application.port.out.history.HistoryOpsPort.SaveResult;
import com.licensis.notaire.config.GlobalExceptionHandler;

@DisplayName("HistoryController ServerError ErrorResponse (issue #579)")
class HistoryControllerServerErrorResponseTest {

    private final ManageHistoriesUseCase manage = mock(ManageHistoriesUseCase.class);
    private final MockMvc mvc = standaloneSetup(new HistoryController(mock(QueryHistoriesUseCase.class), manage))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static final String BODY = "{\"managementStatusId\":1,\"managementId\":1,\"notes\":\"x\"}";

    @Test
    @DisplayName("create ServerError returns 500 ErrorResponse body (#579)")
    void createServerErrorReturnsErrorResponseBody() throws Exception {
        when(manage.create(any())).thenReturn(new SaveResult.ServerError());

        mvc.perform(post("/api/v1/history").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
