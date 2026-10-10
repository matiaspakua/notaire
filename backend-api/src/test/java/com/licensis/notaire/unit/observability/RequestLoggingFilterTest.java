package com.licensis.notaire.unit.observability;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.licensis.notaire.observability.RequestLoggingFilter;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("RequestLoggingFilter")
class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();
    private final Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
        MDC.clear();
    }

    private List<ILoggingEvent> run(MockHttpServletRequest request, int status) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(status);
        filter.doFilter(request, response, new MockFilterChain());
        return appender.list;
    }

    @Test
    @DisplayName("shouldLogRequestWithTraceIdFromMdc")
    void shouldLogRequestWithTraceIdFromMdc() throws Exception {
        MDC.put("traceId", "4bf92f3577b34da6a3ce929d0e0e4736");

        List<ILoggingEvent> events = run(new MockHttpServletRequest("GET", "/api/v1/people"), 200);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getFormattedMessage())
                .contains("http_request", "method=GET", "path=/api/v1/people", "status=200", "durationMs=");
        assertThat(events.get(0).getMDCPropertyMap()).containsEntry("traceId", "4bf92f3577b34da6a3ce929d0e0e4736");
    }

    @Test
    @DisplayName("shouldNotLogQueryString")
    void shouldNotLogQueryString() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/people");
        request.setQueryString("dni=12345678&token=secret");

        List<ILoggingEvent> events = run(request, 200);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getFormattedMessage()).doesNotContain("secret", "dni=", "12345678");
    }

    @Test
    @DisplayName("shouldSkipActuatorPaths")
    void shouldSkipActuatorPaths() throws Exception {
        List<ILoggingEvent> events = run(new MockHttpServletRequest("GET", "/actuator/health"), 200);

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("shouldLogServerErrorsAtWarnLevel")
    void shouldLogServerErrorsAtWarnLevel() throws Exception {
        List<ILoggingEvent> events = run(new MockHttpServletRequest("POST", "/api/v1/deeds"), 500);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getLevel().toString()).isEqualTo("WARN");
    }

    @Test
    @DisplayName("shouldRunInsideTheTraceScopeButBeforeSecurityToLogRejectedRequests")
    void shouldRunInsideTheTraceScopeButBeforeSecurityToLogRejectedRequests() {
        Order order = RequestLoggingFilter.class.getAnnotation(Order.class);

        assertThat(order).isNotNull();
        // Spring's observation filter is HIGHEST_PRECEDENCE + 1; Spring Security's chain is -100.
        assertThat(order.value()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 2);
    }
}
