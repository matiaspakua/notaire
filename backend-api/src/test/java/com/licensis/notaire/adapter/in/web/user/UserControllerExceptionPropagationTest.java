package com.licensis.notaire.adapter.in.web.user;

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

import com.licensis.notaire.application.port.in.user.AuthenticateUserUseCase;
import com.licensis.notaire.application.port.in.user.ManageUsersUseCase;
import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.config.AuthCookieService;
import com.licensis.notaire.config.GlobalExceptionHandler;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.config.TokenRevocationService;
import com.licensis.notaire.observability.MetricsUtil;
import com.licensis.notaire.security.InMemoryLoginAttemptStore;
import com.licensis.notaire.security.LoginAttemptService;

@DisplayName("UserController exception propagation (issue #579)")
class UserControllerExceptionPropagationTest {

    private final QueryUsersUseCase query = mock(QueryUsersUseCase.class);
    private final ManageUsersUseCase manage = mock(ManageUsersUseCase.class);
    private final AuthenticateUserUseCase authenticate = mock(AuthenticateUserUseCase.class);
    private final MockMvc mvc = standaloneSetup(new UserController(
                    query,
                    manage,
                    authenticate,
                    mock(JwtTokenService.class),
                    mock(MetricsUtil.class),
                    new LoginAttemptService(5, 900000, new InMemoryLoginAttemptStore()),
                    mock(AuthCookieService.class),
                    mock(TokenRevocationService.class)))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static final String BODY = "{\"name\": \"newuser\", \"password\": \"secret\", \"type\": \"Notary\", \"active\": true}";

    @Test
    @DisplayName("unexpected create failure returns 500 ErrorResponse via GlobalExceptionHandler (#579)")
    void unexpectedCreateFailureUsesGlobalHandler() throws Exception {
        when(manage.create(any())).thenThrow(new RuntimeException("Database error"));

        mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
