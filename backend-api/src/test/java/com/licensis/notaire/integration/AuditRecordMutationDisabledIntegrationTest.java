package com.licensis.notaire.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Audit-log mutation disabled (issues #1068 / #1060)")
class AuditRecordMutationDisabledIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /audit-log is rejected — audit rows are server-authored only")
    void shouldRejectAuditLogPost() throws Exception {
        mockMvc.perform(post("/api/v1/audit-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "module": "Forged",
                                  "operationDetail": "client must not create audit rows",
                                  "version": 1
                                }
                                """))
                .andExpect(status().isMethodNotAllowed());
    }
}
