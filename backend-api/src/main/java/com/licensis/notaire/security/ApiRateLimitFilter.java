package com.licensis.notaire.security;

import java.io.IOException;
import java.time.Instant;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Quota for {@code /api/**}. The key is the authenticated username when the
 * JWT filter has already run, otherwise the servlet remote address. The
 * counters live in {@code api_rate_windows}, so every API instance shares them
 * (issue #2010).
 */
public class ApiRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiRateLimitFilter.class);

    private final ApiRequestQuota quota;
    private final Supplier<Instant> clock;

    public ApiRateLimitFilter(ApiRequestQuota quota, Supplier<Instant> clock) {
        this.quota = quota;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Instant now = clock.get();
        String key = key(request);
        if (!quota.allow(key, now)) {
            String keyType = key.contains(":") ? key.substring(0, key.indexOf(':')) : key;
            log.warn("API rate limit exceeded: keyType={} path={}", keyType, request.getRequestURI());
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString(quota.retryAfterSeconds(key, now)));
            return;
        }
        chain.doFilter(request, response);
    }

    private static String key(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName())) {
            return "user:" + authentication.getName();
        }
        return "ip:" + request.getRemoteAddr();
    }
}
