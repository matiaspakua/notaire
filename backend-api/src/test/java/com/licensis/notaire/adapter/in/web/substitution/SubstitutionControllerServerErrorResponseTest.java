package com.licensis.notaire.adapter.in.web.substitution;

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

import com.licensis.notaire.application.port.in.substitution.ManageSubstitutionsUseCase;
import com.licensis.notaire.application.port.in.substitution.QuerySubstitutionsUseCase;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.SaveResult;
import com.licensis.notaire.config.GlobalExceptionHandler;

@DisplayName("SubstitutionController ServerError ErrorResponse (issue #579)")
class SubstitutionControllerServerErrorResponseTest {

    private final ManageSubstitutionsUseCase manage = mock(ManageSubstitutionsUseCase.class);
    private final MockMvc mvc = standaloneSetup(new SubstitutionController(mock(QuerySubstitutionsUseCase.class), manage))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static final String BODY =
            "{\"dateStart\":\"2024-01-01T00:00:00.000+00:00\",\"dateEnd\":\"2024-01-31T00:00:00.000+00:00\",\"substitutePersonId\":1,\"substitutedPersonId\":2}";

    @Test
    @DisplayName("create ServerError returns 500 ErrorResponse body (#579)")
    void createServerErrorReturnsErrorResponseBody() throws Exception {
        when(manage.create(any())).thenReturn(new SaveResult.ServerError());

        mvc.perform(post("/api/v1/substitutions").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
