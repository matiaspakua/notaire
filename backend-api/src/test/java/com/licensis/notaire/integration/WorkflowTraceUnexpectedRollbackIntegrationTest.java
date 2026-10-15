package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.licensis.notaire.testing.RequirementCoverage;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.read.ListAppender;

/**
 * #1745: reproduce UnexpectedRollbackException without an outer test TX.
 * Class-level {@code @Transactional} on other H2 tests masks the bug by joining
 * the controller TX to the test TX (no commit at method exit).
 */
@RequirementCoverage({"CU77", "CU83"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("CU77 — workflow-trace 400 path without UnexpectedRollbackException")
class WorkflowTraceUnexpectedRollbackIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private Logger rootLogger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        appender = new ListAppender<>();
        appender.start();
        rootLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        rootLogger.detachAppender(appender);
    }

    @Test
    @DisplayName("Missing management answers 400 without UnexpectedRollbackException ERROR")
    void missingManagementAnswers400WithoutUnexpectedRollback() throws Exception {
        mockMvc.perform(get("/api/v1/managements/{id}/workflow-trace", 999999))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        List<ILoggingEvent> rollbackErrors = appender.list.stream()
                .filter(e -> e.getLevel().isGreaterOrEqual(Level.ERROR))
                .filter(this::mentionsUnexpectedRollback)
                .toList();
        assertThat(rollbackErrors)
                .as("no UnexpectedRollbackException ERROR on expected 400 workflow-trace path")
                .isEmpty();
    }

    private boolean mentionsUnexpectedRollback(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message != null && message.contains("UnexpectedRollbackException")) {
            return true;
        }
        if (event.getThrowableProxy() instanceof ThrowableProxy proxy) {
            Throwable t = proxy.getThrowable();
            while (t != null) {
                if (t.getClass().getName().contains("UnexpectedRollbackException")) {
                    return true;
                }
                t = t.getCause();
            }
            String className = event.getThrowableProxy().getClassName();
            return className != null && className.contains("UnexpectedRollbackException");
        }
        return false;
    }
}
