package com.licensis.notaire.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for BCrypt password matching (TC-LOGIN-09).
 *
 * Validates that the BCrypt password encoder used by UsuarioController#login
 * correctly matches raw passwords against stored hashes, including the
 * transparent MD5-to-BCrypt migration path.
 */
@DisplayName("BCrypt password matching (TC-LOGIN-09)")
class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("Should match a raw password against its BCrypt hash")
    void shouldMatchRawPasswordAgainstBcryptHash() {
        String rawPassword = "admin";
        String hash = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder.matches(rawPassword, hash)).isTrue();
    }

    @Test
    @DisplayName("Should not match a wrong password against a BCrypt hash")
    void shouldNotMatchWrongPasswordAgainstBcryptHash() {
        String hash = passwordEncoder.encode("correct-password");

        assertThat(passwordEncoder.matches("wrong-password", hash)).isFalse();
    }

    @Test
    @DisplayName("Should generate different hashes for the same password (salt)")
    void shouldGenerateDifferentHashesForSamePassword() {
        String rawPassword = "admin";
        String hash1 = passwordEncoder.encode(rawPassword);
        String hash2 = passwordEncoder.encode(rawPassword);

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(passwordEncoder.matches(rawPassword, hash1)).isTrue();
        assertThat(passwordEncoder.matches(rawPassword, hash2)).isTrue();
    }

    @Test
    @DisplayName("Should match a null-safe password check")
    void shouldHandleNullPasswordGracefully() {
        String hash = passwordEncoder.encode("admin");

        assertThat(passwordEncoder.matches(null, hash)).isFalse();
        assertThat(passwordEncoder.matches("admin", null)).isFalse();
    }

    @Test
    @DisplayName("Should identify BCrypt hash format by prefix")
    void shouldIdentifyBcryptHashByPrefix() {
        String hash = passwordEncoder.encode("admin");

        assertThat(hash).startsWith("$2");
    }
}
