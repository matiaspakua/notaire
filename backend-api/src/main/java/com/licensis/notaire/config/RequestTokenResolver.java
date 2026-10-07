package com.licensis.notaire.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Reads the JWT presented by a request: {@code Authorization: Bearer} first
 * (API tooling), then the HttpOnly session cookie (browser) — issue #1051.
 * Shared by the authentication filter and logout (issue #676).
 */
public final class RequestTokenResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private RequestTokenResolver() {
    }

    public static String resolve(HttpServletRequest request, String cookieName) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String bearer = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (!bearer.isEmpty()) {
                return bearer;
            }
        }
        return readCookieToken(request, cookieName);
    }

    private static String readCookieToken(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookieName == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                String value = cookie.getValue();
                return (value == null || value.isBlank()) ? null : value;
            }
        }
        return null;
    }
}
