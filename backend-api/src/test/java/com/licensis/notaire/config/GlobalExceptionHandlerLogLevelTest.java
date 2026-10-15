package com.licensis.notaire.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.licensis.notaire.exception.NotaireException;
import com.licensis.notaire.observability.StructuredLogger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * #1712: expected client failures (HTTP 4xx) must not emit ERROR structured logs.
 */
class GlobalExceptionHandlerLogLevelTest {

    @RestController
    static class ProbeController {
        @PostMapping("/conflict")
        void conflict() {
            throw new NotaireException(409, "Identification type name already exists");
        }

        @DeleteMapping("/server-fault")
        void serverFault() {
            throw new NotaireException(500, "Unexpected failure");
        }
    }

    private final MockMvc mvc = standaloneSetup(new ProbeController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private Logger structuredLogger;
    private Level originalLevel;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        structuredLogger = (Logger) LoggerFactory.getLogger(StructuredLogger.class);
        originalLevel = structuredLogger.getLevel();
        structuredLogger.setLevel(Level.TRACE);
        appender = new ListAppender<>();
        appender.start();
        structuredLogger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        structuredLogger.detachAppender(appender);
        structuredLogger.setLevel(originalLevel);
    }

    @Test
    @DisplayName("409 NotaireException is logged at WARN, not ERROR")
    void conflictIsWarn() throws Exception {
        mvc.perform(post("/conflict")).andExpect(status().isConflict());

        List<ILoggingEvent> events = appender.list.stream()
                .filter(e -> e.getFormattedMessage().contains("Notaire"))
                .toList();
        assertThat(events).isNotEmpty();
        assertThat(events.get(events.size() - 1).getLevel()).isEqualTo(Level.WARN);
    }

    @Test
    @DisplayName("500 NotaireException is still logged at ERROR")
    void serverFaultIsError() throws Exception {
        mvc.perform(delete("/server-fault")).andExpect(status().isInternalServerError());

        List<ILoggingEvent> events = appender.list.stream()
                .filter(e -> e.getFormattedMessage().contains("Notaire"))
                .toList();
        assertThat(events).isNotEmpty();
        assertThat(events.get(events.size() - 1).getLevel()).isEqualTo(Level.ERROR);
    }
}
