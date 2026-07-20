package com.licensis.notaire.config;

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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security and CORS configuration for the application.
 * Provides centralized configuration for security settings including
 * Actuator endpoint protection with admin/admin credentials for Prometheus scraping.
 */
@Configuration
@EnableWebSecurity
public class SecurityAndCorsConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityAndCorsConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String[] allowedMethods;

    @Value("${cors.max-age:3600}")
    private long maxAge;

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
     * This overrides Spring Boot auto-configuration from spring.security.user.* properties.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin"))
            .roles("ACTUATOR", "ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }

    /**
     * Security filter chain for Actuator endpoints.
     * Protects Prometheus metrics and health endpoints with HTTP Basic auth.
     * Credentials: admin / admin
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
            .csrf(csrf -> csrf.disable())
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
     * Security filter chain for API endpoints.
     * JWT filter authenticates requests that carry a valid Bearer token.
     * Only the login endpoint (and CORS preflight) is reachable without one;
     * every other /api/** request must present a valid token.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
            AuthenticationEntryPoint apiAuthenticationEntryPoint) throws Exception {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/login").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(apiAuthenticationEntryPoint))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(withDefaults());
        return http.build();
    }

    /**
     * Security filter chain for all other endpoints (Swagger, etc.)
     */
    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .anyRequest().permitAll()
            )
            .httpBasic(withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(withDefaults());
        return http.build();
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
                    .allowedHeaders("*");

                registry.addMapping("/actuator/**")
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods(allowedMethods)
                    .allowCredentials(true)
                    .maxAge(maxAge)
                    .allowedHeaders("*");
            }
        };
    }
}
