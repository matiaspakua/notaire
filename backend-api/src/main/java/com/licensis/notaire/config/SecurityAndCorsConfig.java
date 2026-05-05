package com.licensis.notaire.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

/**
 * Spring Security and CORS configuration for the application
 * Provides centralized configuration for security settings
 */
@Configuration
public class SecurityAndCorsConfig {

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
            }
        };
    }
}
