package com.licensis.notaire.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #655 (Wave 9): write controllers that lacked {@code @Valid} must answer 400 on an empty
 * or invalid JSON body before business logic runs.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Remaining write controllers reject empty bodies (#655)")
class RemainingWriteControllersValidationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @ParameterizedTest(name = "POST {0} with empty body answers 400")
    @CsvSource({
        "/api/v1/folio-types",
        "/api/v1/document-types",
        "/api/v1/management-statuses",
        "/api/v1/procedure-types",
        "/api/v1/workflow-definition",
        "/api/v1/workflow-node",
        "/api/v1/workflow-transition",
        "/api/v1/testimony-movements",
        "/api/v1/notebooks",
        "/api/v1/auxiliary-protocol/deeds"
    })
    @DisplayName("POST create with {} answers 400")
    void postEmptyBodyIsBadRequest(String url) throws Exception {
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest(name = "PUT {0}/1 with empty body answers 400")
    @CsvSource({
        "/api/v1/folio-types",
        "/api/v1/document-types",
        "/api/v1/management-statuses",
        "/api/v1/procedure-types",
        "/api/v1/workflow-definition",
        "/api/v1/workflow-transition"
    })
    @DisplayName("PUT update with {} answers 400 when name or required ids are missing")
    void putEmptyBodyIsBadRequest(String url) throws Exception {
        mockMvc.perform(put(url + "/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
