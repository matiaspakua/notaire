package com.licensis.notaire.config;

import com.licensis.notaire.security.RoleModuleAuthorizationFilter;
import com.licensis.notaire.security.UserAuthorityResolver;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security and CORS configuration for the application.
 * Provides centralized configuration for security settings including
 * Actuator endpoint protection with configurable credentials for Prometheus scraping.
 */
@Configuration
@EnableWebSecurity
public class SecurityAndCorsConfig {

    private static final String PRODUCTION_ENVIRONMENT = "production";

    private static final String[] ADMIN_ONLY_PATHS = {
        "/api/v1/users/**",
        "/api/v1/roles/**",
        "/api/v1/audit-log/**"
    };

    private static final String[] ADMIN_WRITE_PATHS = {
        "/api/v1/workflow-definition/**",
        "/api/v1/workflow-node/**",
        "/api/v1/workflow-transition/**",
        "/api/v1/procedure-types/**",
        "/api/v1/document-types/**",
        "/api/v1/folio-types/**",
        "/api/v1/identification-types/**",
        "/api/v1/concepts/**",
        "/api/v1/management-statuses/**",
        "/api/v1/procedure-templates/**",
        "/api/v1/budget-templates/**",
        "/api/v1/document-cost-templates/**"
    };

    /**
     * The management history is the audit trail of state changes (CU13): any authenticated
     * user may read it or record a new row, but only an administrator may rewrite or delete
     * one (issue #1250).
     */
    private static final String HISTORY_ENTRY_PATH = "/api/v1/history/*";

    private static final HttpMethod[] ADMIN_HISTORY_METHODS = {HttpMethod.PUT, HttpMethod.DELETE};

    private static final HttpMethod[] WRITE_METHODS = {
        HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE
    };

    /**
     * Keeps the double-submit nonce stable across authenticated requests (issue #1620).
     *
     * <p>Spring Security's default {@code CsrfAuthenticationStrategy} deletes the
     * {@code XSRF-TOKEN} cookie and issues a fresh value on every authenticated
     * request. That is a session-fixation defence for a token bound to a session;
     * here the token is a stateless nonce that the same client echoes back, so
     * rotating it only races the client: a response completing between the
     * client's {@code document.cookie} read and the request being sent makes the
     * header and the cookie disagree, and the API answers 403 on the next write.
     * The double-submit check itself is unaffected — the nonce is never a
     * credential and is still required to match.</p>
     */
    private static final SessionAuthenticationStrategy KEEP_CSRF_TOKEN_ON_AUTHENTICATION =
            (authentication, request, response) -> {
                // Intentionally empty: nothing to rotate for a stateless nonce.
            };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RoleModuleAuthorizationFilter roleModuleAuthorizationFilter;

    public SecurityAndCorsConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
            RoleModuleAuthorizationFilter roleModuleAuthorizationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.roleModuleAuthorizationFilter = roleModuleAuthorizationFilter;
    }

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String[] allowedMethods;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Value("${cors.allowed-headers:Content-Type,Authorization,X-XSRF-TOKEN}")
    private String[] allowedHeaders;

    @Value("${actuator.security.username}")
    private String actuatorUsername;

    @Value("${actuator.security.password}")
    private String actuatorPassword;

    @Value("${app.environment:development}")
    private String environment;

    /**
     * Password encoder bean using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * User details service for actuator and admin users.
     * Stores password as BCrypt hash for the admin user.
     * Credentials come from actuator.security.username/password (bound to the
     * ACTUATOR_USER/ACTUATOR_PASSWORD env vars, see .env.example) rather than
     * being hardcoded, so they can be changed without a code change (issue #557).
     * This overrides Spring Boot auto-configuration from spring.security.user.* properties.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var admin = User.builder()
            .username(actuatorUsername)
            .password(passwordEncoder.encode(actuatorPassword))
            .roles("ACTUATOR", "ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }

    /**
     * Security filter chain for Actuator endpoints.
     * Protects Prometheus metrics and health endpoints with HTTP Basic auth.
     * Credentials: actuator.security.username / actuator.security.password.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(withDefaults())
            .cors(withDefaults());
        return http.build();
    }

    /**
     * Authentication entry point for unauthenticated API requests.
     * Returns a plain 401 instead of Spring Security's default redirect/challenge
     * behavior, since the API is consumed by a JS client, not a browser login form.
     */
    @Bean
    public AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        return (request, response, authException) ->
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
    }

    /**
     * Plain 403 for authenticated users without the required authority (issue #559).
     */
    @Bean
    public AccessDeniedHandler apiAccessDeniedHandler() {
        return (request, response, accessDeniedException) ->
            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden");
    }

    /**
     * Security filter chain for API endpoints.
     * JWT filter authenticates from Bearer or the HttpOnly session cookie (#1051).
     * Login and logout (cookie clear) are reachable without prior auth; every other
     * /api/** request must present a valid credential.
     *
     * <p>CSRF protection uses the double-submit cookie pattern (CU78 / #1618):
     * the {@code XSRF-TOKEN} cookie is set on every response and the browser BFF
     * echoes the cookie value back as the {@code X-XSRF-TOKEN} request header
     * (see {@code frontend/src/lib/api-client.ts}). Because the header carries a
     * value the client read from the cookie, the API chain resolves
     * header-supplied tokens verbatim via
     * {@link org.springframework.security.config.annotation.web.configurers.CsrfConfigurer#spa()}
     * instead of Spring Security's XOR-masked default (regression from #1619,
     * issue #1620).</p>
     *
     * <p>Bearer-token clients (Bruno, mobile, server-to-server) are exempt
     * because they are not vulnerable to CSRF — only ambient cookie-authenticated
     * browser flows are (issue #1620).</p>
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
            AuthenticationEntryPoint apiAuthenticationEntryPoint,
            AccessDeniedHandler apiAccessDeniedHandler) throws Exception {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/api/v1/users/login").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/api/v1/users/logout").permitAll();
                auth.requestMatchers(ADMIN_ONLY_PATHS).hasRole(UserAuthorityResolver.ADMIN_ROLE);
                for (HttpMethod method : WRITE_METHODS) {
                    auth.requestMatchers(method, ADMIN_WRITE_PATHS).hasRole(UserAuthorityResolver.ADMIN_ROLE);
                }
                for (HttpMethod method : ADMIN_HISTORY_METHODS) {
                    auth.requestMatchers(method, HISTORY_ENTRY_PATH).hasRole(UserAuthorityResolver.ADMIN_ROLE);
                }
                auth.anyRequest().authenticated();
            })
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(apiAuthenticationEntryPoint)
                .accessDeniedHandler(apiAccessDeniedHandler))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(roleModuleAuthorizationFilter, JwtAuthenticationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf
                // Framework defaults for a single-page app (issue #1620): the
                // non-HttpOnly XSRF-TOKEN cookie plus a request handler that resolves
                // a header-supplied token verbatim instead of expecting the XOR mask
                // meant for server-rendered forms (CsrfConfigurer.spa()).
                .spa()
                // Spring Security replaces the CSRF cookie on every authenticated
                // request (CsrfAuthenticationStrategy). That defends a
                // session-scoped token against fixation; this nonce is not
                // session-scoped, so rotation only churns the cookie and races with
                // the client's read-then-send, leaving the echoed header and the
                // browser cookie disagreeing (403) while the dashboard fires
                // parallel requests.
                .sessionAuthenticationStrategy(KEEP_CSRF_TOKEN_ON_AUTHENTICATION)
                // Exempt login/logout (session bootstrap) and Bearer-token API clients.
                // Bearer tokens are not vulnerable to CSRF — CSRF only affects ambient
                // cookie-authenticated browser requests. See CU78 / issues #1618, #1620.
                // CSRF stays enabled for cookie SPA clients (.spa() above); this is not a global disable.
                .ignoringRequestMatchers( // NOSONAR java:S4502 — scoped Bearer/login exemptions only (#1620)
                    request -> {
                        String auth = request.getHeader("Authorization");
                        return auth != null && auth.startsWith("Bearer ");
                    },
                    request -> {
                        String path = request.getRequestURI();
                        return path.equals("/api/v1/users/login") || path.equals("/api/v1/users/logout");
                    }
                ))
            .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
            .cors(withDefaults());
        return http.build();
    }

    /**
     * Security filter chain for all other endpoints (Swagger, etc.)
     * Swagger UI and the OpenAPI spec are publicly reachable in every environment
     * except production, where they are denied to avoid exposing the API surface
     * and a live "Try it out" console to anonymous visitors (issue #671).
     */
    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> {
                var swaggerPaths = auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html");
                if (isProduction()) {
                    swaggerPaths.denyAll();
                } else {
                    swaggerPaths.permitAll();
                }
                auth.anyRequest().permitAll();
            })
            .httpBasic(withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(withDefaults())
            .cors(withDefaults());
        return http.build();
    }

    private boolean isProduction() {
        return PRODUCTION_ENVIRONMENT.equalsIgnoreCase(environment);
    }

    /**
     * Materializes the deferred token before the handler writes the response so
     * the BFF can relay the non-HttpOnly double-submit cookie without exposing
     * the HttpOnly JWT. Materializing after the chain would rely on the response
     * not being committed yet, which is not guaranteed for large bodies
     * (for example the PDF downloads served by {@code apiGetBytes}).
     */
    private static final class CsrfCookieFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Refuses to start in production with a wildcard CORS configuration.
     * A wildcard {@code Access-Control-Allow-Headers} (including {@code Authorization})
     * combined with an unrestricted origin lets any site read authenticated
     * responses cross-origin (issue #673).
     */
    @PostConstruct
    public void validateProductionCorsConfig() {
        if (!isProduction()) {
            return;
        }
        if (Arrays.asList(allowedHeaders).contains("*")) {
            throw new IllegalStateException(
                    "cors.allowed-headers cannot be '*' in production. "
                    + "Configure an explicit allow-list of headers.");
        }
        if (Arrays.asList(allowedOrigins).contains("*")) {
            throw new IllegalStateException(
                    "cors.allowed-origins cannot be '*' in production. "
                    + "Configure the frontend origins explicitly.");
        }
    }

    /**
     * CORS configuration
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods(allowedMethods)
                    .allowCredentials(true)
                    .maxAge(maxAge)
                    .allowedHeaders(allowedHeaders);

                registry.addMapping("/actuator/**")
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods(allowedMethods)
                    .allowCredentials(true)
                    .maxAge(maxAge)
                    .allowedHeaders(allowedHeaders);
            }
        };
    }
}
