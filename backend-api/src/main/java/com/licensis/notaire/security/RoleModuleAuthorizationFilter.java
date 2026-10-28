package com.licensis.notaire.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Denies authenticated API calls when the caller's role lacks the module mapped to
 * the request path (ADR-029 W2 / issue #1683).
 */
@Component
public class RoleModuleAuthorizationFilter extends OncePerRequestFilter {

    private final RoleModuleGrantService roleModuleGrantService;

    public RoleModuleAuthorizationFilter(RoleModuleGrantService roleModuleGrantService) {
        this.roleModuleGrantService = roleModuleGrantService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Optional<String> requiredModule = ApiModulePathRegistry.requiredModule(request.getRequestURI());
        if (requiredModule.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof String username
                    && !roleModuleGrantService.isGranted(username, requiredModule.get())) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
