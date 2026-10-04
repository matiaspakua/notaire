package com.licensis.notaire.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.licensis.notaire.security.UserAuthorityResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authenticates API requests from either {@code Authorization: Bearer} (API
 * tooling) or the HttpOnly session cookie (browser via Next proxy) — issue #1051.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final AuthCookieService authCookieService;
    private final UserAuthorityResolver userAuthorityResolver;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   AuthCookieService authCookieService,
                                   UserAuthorityResolver userAuthorityResolver) {
        this.jwtTokenService = jwtTokenService;
        this.authCookieService = authCookieService;
        this.userAuthorityResolver = userAuthorityResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && jwtTokenService.isValid(token)) {
            String username = jwtTokenService.extractUsername(token);
            userAuthorityResolver.resolve(username).ifPresent(authorities ->
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(username, null, authorities)));
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String bearer = authHeader.substring(7).trim();
            if (!bearer.isEmpty()) {
                return bearer;
            }
        }
        return readCookieToken(request);
    }

    private String readCookieToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        String name = authCookieService.cookieName();
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                String value = cookie.getValue();
                return (value == null || value.isBlank()) ? null : value;
            }
        }
        return null;
    }
}
