package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU78"})
@DisplayName("JwtTokenService scoped MFA tokens (CU78, ADR-032)")
class JwtScopedTokenTest {

    private JwtTokenService jwt;

    @BeforeEach
    void setUp() {
        jwt = new JwtTokenService();
        ReflectionTestUtils.setField(jwt, "secretKey", "notaire-test-secret-key-minimum-32-bytes-for-hs256!!");
        ReflectionTestUtils.setField(jwt, "expirationMs", 3600000L);
    }

    @Test
    @DisplayName("a scoped token is never a valid session token")
    void scopedTokenIsNotASession() {
        String token = jwt.generateScopedToken("admin", "mfa_challenge", 60_000L);
        assertThat(jwt.isValid(token)).isFalse();
    }

    @Test
    @DisplayName("a scoped token validates only for its own scope and returns its subject")
    void scopedTokenValidatesForItsScope() {
        String token = jwt.generateScopedToken("admin", "mfa_challenge", 60_000L);
        assertThat(jwt.scopedSubject(token, "mfa_challenge")).contains("admin");
        assertThat(jwt.scopedSubject(token, "mfa_enrollment")).isEmpty();
    }

    @Test
    @DisplayName("a session token has no scope and an expired scoped token is refused")
    void sessionAndExpiredTokens() {
        assertThat(jwt.scopedSubject(jwt.generateToken("admin"), "mfa_challenge")).isEmpty();
        assertThat(jwt.scopedSubject(jwt.generateScopedToken("admin", "mfa_challenge", -1_000L), "mfa_challenge"))
                .isEmpty();
        assertThat(jwt.scopedSubject("garbage", "mfa_challenge")).isEmpty();
    }
}
