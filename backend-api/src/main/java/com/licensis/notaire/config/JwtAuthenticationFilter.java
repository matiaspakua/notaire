package com.licensis.notaire.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.licensis.notaire.security.UserAuthorityResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates API requests from either {@code Authorization: Bearer} (API
 * tooling) or the HttpOnly session cookie (browser via Next proxy) — issue #1051.
 * Tokens revoked by logout are ignored (issue #676).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final AuthCookieService authCookieService;
    private final UserAuthorityResolver userAuthorityResolver;
    private final TokenRevocationService tokenRevocationService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   AuthCookieService authCookieService,
                                   UserAuthorityResolver userAuthorityResolver,
                                   TokenRevocationService tokenRevocationService) {
        this.jwtTokenService = jwtTokenService;
        this.authCookieService = authCookieService;
        this.userAuthorityResolver = userAuthorityResolver;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = RequestTokenResolver.resolve(request, authCookieService.cookieName());
        if (token != null && jwtTokenService.isValid(token) && !tokenRevocationService.isRevoked(token)) {
            String username = jwtTokenService.extractUsername(token);
            userAuthorityResolver.resolve(username).ifPresent(authorities ->
                    authenticate(username, authorities));
        } else if (token != null && MfaTokenScope.isEnrollmentPath(request.getRequestURI())) {
            jwtTokenService.scopedSubject(token, MfaTokenScope.ENROLLMENT)
                    .filter(username -> userAuthorityResolver.resolve(username).isPresent())
                    .ifPresent(username -> authenticate(username,
                            List.of(new SimpleGrantedAuthority(MfaTokenScope.ENROLLMENT_AUTHORITY))));
        }
        filterChain.doFilter(request, response);
    }

    private static void authenticate(String username, List<? extends GrantedAuthority> authorities) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, authorities));
    }
}
