package com.licensis.notaire.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class ApiRequestQuotaTest {

    @Test
    @DisplayName("a burst past the quota is rejected, then a later call in the next window is allowed")
    void burstThenWindow() {
        Instant start = Instant.parse("2026-11-19T08:00:00Z");
        ApiRequestQuota quota = new ApiRequestQuota(2, Duration.ofSeconds(60));

        assertThat(quota.allow("10.0.0.8", start)).isTrue();
        assertThat(quota.allow("10.0.0.8", start.plusSeconds(1))).isTrue();
        assertThat(quota.allow("10.0.0.8", start.plusSeconds(2))).isFalse();
        assertThat(quota.retryAfterSeconds("10.0.0.8", start.plusSeconds(2))).isEqualTo(58);
        assertThat(quota.allow("10.0.0.9", start.plusSeconds(2))).isTrue();
        assertThat(quota.allow("10.0.0.8", start.plusSeconds(60))).isTrue();
    }

    @Test
    @DisplayName("the filter answers 429 and does not call the chain when the quota is spent")
    void filterRejects() throws Exception {
        ApiRequestQuota quota = new ApiRequestQuota(1, Duration.ofSeconds(60));
        ApiRateLimitFilter filter = new ApiRateLimitFilter(quota, () -> Instant.parse("2026-11-19T08:00:00Z"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/people");
        request.setRemoteAddr("10.1.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((MockHttpServletResponse) res).setStatus(200);

        filter.doFilter(request, response, chain);
        assertThat(response.getStatus()).isEqualTo(200);

        MockHttpServletResponse blocked = new MockHttpServletResponse();
        boolean[] called = {false};
        filter.doFilter(request, blocked, (req, res) -> called[0] = true);
        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isEqualTo("60");
        assertThat(called[0]).isFalse();
    }
}
