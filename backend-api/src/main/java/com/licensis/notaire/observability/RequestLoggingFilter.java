package com.licensis.notaire.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Writes one structured line per API request. It runs inside the trace scope, so the JSON log line
 * carries {@code traceId}/{@code spanId} (see logback-spring.xml). Query strings, headers and bodies
 * are never logged because they may carry identifiers or tokens (issue #1463).
 *
 * <p>Ordered just inside Spring's observation filter (HIGHEST_PRECEDENCE + 1, which opens the span) and
 * before Spring Security, so requests rejected with 401/403 are logged too.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String ACTUATOR_PREFIX = "/actuator";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith(ACTUATOR_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            logRequest(request, response, (System.nanoTime() - startNanos) / 1_000_000);
        }
    }

    private void logRequest(HttpServletRequest request, HttpServletResponse response, long durationMs) {
        int status = response.getStatus();
        String line = "http_request method={} path={} status={} durationMs={}";
        if (status >= 500) {
            LOG.warn(line, request.getMethod(), request.getRequestURI(), status, durationMs);
        } else {
            LOG.info(line, request.getMethod(), request.getRequestURI(), status, durationMs);
        }
    }
}
