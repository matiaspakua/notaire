package com.licensis.notaire.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;

class ApiRateLimitFilter429LogTest {

    private static final String USERNAME = "alice.secret";
    private static final String IP = "203.0.113.77";

    private final Logger logger = (Logger) LoggerFactory.getLogger(ApiRateLimitFilter.class);
    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    @BeforeEach
    void setUp() {
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.list.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("429 for an authenticated user logs WARN with key type and path, never the username")
    void log429ForUserKey() throws Exception {
        Instant now = Instant.parse("2026-11-19T08:00:00Z");
        ApiRequestQuota quota = new ApiRequestQuota(1, Duration.ofSeconds(60));
        quota.allow("user:" + USERNAME, now); // exhaust the quota

        ApiRateLimitFilter filter = new ApiRateLimitFilter(quota, () -> now);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(USERNAME, "password", Collections.emptyList()));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/people");
        request.setRemoteAddr(IP);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {};

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);

        List<ILoggingEvent> warnEvents = listAppender.list.stream()
                .filter(e -> e.getLevel() == Level.WARN)
                .toList();
        assertThat(warnEvents).isNotEmpty();
        String message = warnEvents.get(0).getFormattedMessage();
        assertThat(message).contains("user");
        assertThat(message).contains("/api/v1/people");
        assertThat(message).doesNotContain(USERNAME);
        assertThat(message).doesNotContain(IP);
    }

    @Test
    @DisplayName("429 for an anonymous IP logs WARN with key type and path, never the IP address")
    void log429ForIpKey() throws Exception {
        Instant now = Instant.parse("2026-11-19T08:00:00Z");
        ApiRequestQuota quota = new ApiRequestQuota(1, Duration.ofSeconds(60));
        quota.allow("ip:" + IP, now); // exhaust the quota

        ApiRateLimitFilter filter = new ApiRateLimitFilter(quota, () -> now);

        SecurityContextHolder.clearContext();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/acts");
        request.setRemoteAddr(IP);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {};

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);

        List<ILoggingEvent> warnEvents = listAppender.list.stream()
                .filter(e -> e.getLevel() == Level.WARN)
                .toList();
        assertThat(warnEvents).isNotEmpty();
        String message = warnEvents.get(0).getFormattedMessage();
        assertThat(message).contains("ip");
        assertThat(message).contains("/api/v1/acts");
        assertThat(message).doesNotContain(IP);
    }
}
