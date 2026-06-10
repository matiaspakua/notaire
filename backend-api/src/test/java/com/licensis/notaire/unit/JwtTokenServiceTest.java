package com.licensis.notaire.unit;

import com.licensis.notaire.config.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenService unit tests")
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService();
        ReflectionTestUtils.setField(jwtTokenService, "secretKey",
                "notaire-test-secret-key-minimum-32-bytes-for-hs256!!");
        ReflectionTestUtils.setField(jwtTokenService, "expirationMs", 3600000L);
    }

    @Test
    @DisplayName("Should generate a non-empty JWT token")
    void shouldGenerateNonEmptyToken() {
        String token = jwtTokenService.generateToken("admin");
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should extract username from generated token")
    void shouldExtractUsernameFromToken() {
        String token = jwtTokenService.generateToken("admin");
        assertThat(jwtTokenService.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("Should validate a freshly generated token as valid")
    void shouldValidateFreshToken() {
        String token = jwtTokenService.generateToken("testuser");
        assertThat(jwtTokenService.isValid(token)).isTrue();
    }

    @Test
    @DisplayName("Should reject a garbage string as invalid")
    void shouldRejectGarbageToken() {
        assertThat(jwtTokenService.isValid("not.a.jwt")).isFalse();
        assertThat(jwtTokenService.isValid("")).isFalse();
        assertThat(jwtTokenService.isValid(null)).isFalse();
    }

    @Test
    @DisplayName("Should reject an expired token")
    void shouldRejectExpiredToken() throws InterruptedException {
        JwtTokenService expiredService = new JwtTokenService();
        ReflectionTestUtils.setField(expiredService, "secretKey",
                "notaire-test-secret-key-minimum-32-bytes-for-hs256!!");
        ReflectionTestUtils.setField(expiredService, "expirationMs", 1L);

        String token = expiredService.generateToken("admin");
        Thread.sleep(10);
        assertThat(expiredService.isValid(token)).isFalse();
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        String tokenA = jwtTokenService.generateToken("userA");
        String tokenB = jwtTokenService.generateToken("userB");

        assertThat(tokenA).isNotEqualTo(tokenB);
        assertThat(jwtTokenService.extractUsername(tokenA)).isEqualTo("userA");
        assertThat(jwtTokenService.extractUsername(tokenB)).isEqualTo("userB");
    }
}
