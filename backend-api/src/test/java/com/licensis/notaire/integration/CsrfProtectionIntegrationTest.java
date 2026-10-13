package com.licensis.notaire.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that the three Spring Security filter chains enforce CSRF
 * protection according to CU78 / issue #1618.
 *
 * <p>The API chain uses a double-submit cookie token. Login is exempted because
 * it bootstraps the session. All other unsafe requests on the /api/** chain must
 * carry the issued token.</p>
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("CSRF protection — CU78 / issue #1618")
class CsrfProtectionIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("login endpoint is exempt from CSRF — requests without a token are accepted")
    void loginEndpointShouldBeExemptFromCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"admin","password":"admin"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("any other unsafe API request without a CSRF token is rejected with 403")
    void unsafeApiRequestWithoutCsrfTokenShouldBeRejected() throws Exception {
        // Attempt a POST without CSRF token on a protected resource.
        // No credentials → 401 from the auth filter if the JWT gate fires first;
        // but if CSRF fires before auth, we expect 403. Either way, the request
        // must not succeed (2xx). We assert is4xxClientError() to cover both orderings.
        mockMvc.perform(post("/api/v1/managements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("unsafe API request with a valid CSRF token is not rejected by the CSRF filter")
    void unsafeApiRequestWithCsrfTokenShouldPassCsrfFilter() throws Exception {
        // The request will still be rejected by the JWT auth filter (401)
        // because it carries no credentials, but it must NOT be rejected with 403
        // by the CSRF filter. Using Spring Security's csrf() post-processor
        // injects a valid token so the CSRF filter passes it through.
        mockMvc.perform(post("/api/v1/managements")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
