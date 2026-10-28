package com.licensis.notaire.unit;

import com.licensis.notaire.config.JwtAuthenticationFilter;
import com.licensis.notaire.config.SecurityAndCorsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("SecurityAndCorsConfig — actuator credentials (issue #557)")
class SecurityAndCorsConfigTest {

    private SecurityAndCorsConfig config;
    private final PasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        config = new SecurityAndCorsConfig(mock(JwtAuthenticationFilter.class));
    }

    @Test
    @DisplayName("userDetailsService should use the configured username, not a hardcoded literal")
    void userDetailsServiceShouldUseConfiguredUsername() {
        ReflectionTestUtils.setField(config, "actuatorUsername", "custom-user");
        ReflectionTestUtils.setField(config, "actuatorPassword", "custom-pass");

        UserDetailsService service = config.userDetailsService(passwordEncoder);

        UserDetails user = service.loadUserByUsername("custom-user");
        assertThat(user.getUsername()).isEqualTo("custom-user");
        assertThat(passwordEncoder.matches("custom-pass", user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("userDetailsService should store a BCrypt-encoded hash, never the plaintext password")
    void userDetailsServiceShouldStoreBcryptHash() {
        ReflectionTestUtils.setField(config, "actuatorUsername", "admin");
        ReflectionTestUtils.setField(config, "actuatorPassword", "s3cret");

        UserDetails user = config.userDetailsService(passwordEncoder).loadUserByUsername("admin");

        assertThat(user.getPassword()).isNotEqualTo("s3cret").startsWith("$2");
    }

    @Test
    @DisplayName("production startup should fail when allowedHeaders is a wildcard (issue #673)")
    void shouldRejectWildcardAllowedHeadersInProduction() {
        ReflectionTestUtils.setField(config, "environment", "production");
        ReflectionTestUtils.setField(config, "allowedHeaders", new String[] {"*"});
        ReflectionTestUtils.setField(config, "allowedOrigins", new String[] {"https://notaire.example.com"});

        org.assertj.core.api.Assertions.assertThatThrownBy(config::validateProductionCorsConfig)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cors.allowed-headers");
    }

    @Test
    @DisplayName("production startup should fail when allowedOrigins is a wildcard (issue #673)")
    void shouldRejectWildcardAllowedOriginsInProduction() {
        ReflectionTestUtils.setField(config, "environment", "production");
        ReflectionTestUtils.setField(config, "allowedHeaders", new String[] {"Content-Type", "Authorization"});
        ReflectionTestUtils.setField(config, "allowedOrigins", new String[] {"*"});

        org.assertj.core.api.Assertions.assertThatThrownBy(config::validateProductionCorsConfig)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cors.allowed-origins");
    }

    @Test
    @DisplayName("production startup should succeed when CORS is explicitly locked down (issue #673)")
    void shouldAllowExplicitCorsConfigInProduction() {
        ReflectionTestUtils.setField(config, "environment", "production");
        ReflectionTestUtils.setField(config, "allowedHeaders", new String[] {"Content-Type", "Authorization"});
        ReflectionTestUtils.setField(config, "allowedOrigins", new String[] {"https://notaire.example.com"});

        org.assertj.core.api.Assertions.assertThatCode(config::validateProductionCorsConfig)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("non-production environments may keep the wildcard CORS defaults (issue #673)")
    void shouldAllowWildcardCorsOutsideProduction() {
        ReflectionTestUtils.setField(config, "environment", "development");
        ReflectionTestUtils.setField(config, "allowedHeaders", new String[] {"*"});
        ReflectionTestUtils.setField(config, "allowedOrigins", new String[] {"*"});

        org.assertj.core.api.Assertions.assertThatCode(config::validateProductionCorsConfig)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("cors.allowed-headers default should not allow-list the dead X-Notaire-User header (issue #731)")
    void allowedHeadersDefaultShouldNotIncludeDeadNotaireUserHeader() throws NoSuchFieldException {
        Field field = SecurityAndCorsConfig.class.getDeclaredField("allowedHeaders");
        Value value = field.getAnnotation(Value.class);

        assertThat(value.value()).doesNotContain("X-Notaire-User");
    }
}
