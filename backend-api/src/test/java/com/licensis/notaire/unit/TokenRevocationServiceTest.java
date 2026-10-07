package com.licensis.notaire.unit;

import com.licensis.notaire.business.RevokedToken;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.config.TokenRevocationService;
import com.licensis.notaire.repository.RevokedTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenRevocationService — per-token logout revocation (issue #676)")
class TokenRevocationServiceTest {

    private static final String SECRET = "notaire-test-secret-key-minimum-32-bytes-for-hs256!!";
    private static final Instant NOW = Instant.parse("2026-10-07T18:00:00Z");

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    private JwtTokenService jwtTokenService;
    private TokenRevocationService service;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService();
        ReflectionTestUtils.setField(jwtTokenService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtTokenService, "expirationMs", 3600000L);
        service = new TokenRevocationService(jwtTokenService, revokedTokenRepository, () -> NOW);
    }

    @Test
    @DisplayName("revoke stores the token id until the token expires and purges expired entries")
    void revokeStoresTokenIdUntilExpiry() {
        String token = jwtTokenService.generateToken("admin");

        service.revoke(token);

        ArgumentCaptor<RevokedToken> saved = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository).save(saved.capture());
        assertThat(saved.getValue().getJti()).isEqualTo(jwtTokenService.extractTokenId(token));
        assertThat(saved.getValue().getExpiresAt()).isEqualTo(jwtTokenService.extractExpiration(token));
        verify(revokedTokenRepository).deleteExpiredBefore(NOW);
    }

    @Test
    @DisplayName("revoke ignores a missing, malformed or forged token")
    void revokeIgnoresInvalidTokens() {
        service.revoke(null);
        service.revoke("");
        service.revoke("not-a-jwt");

        verifyNoInteractions(revokedTokenRepository);
    }

    @Test
    @DisplayName("isRevoked is true only for a stored token id")
    void isRevokedChecksStoredIds() {
        String revoked = jwtTokenService.generateToken("admin");
        String active = jwtTokenService.generateToken("admin");
        when(revokedTokenRepository.existsById(jwtTokenService.extractTokenId(revoked))).thenReturn(true);
        when(revokedTokenRepository.existsById(jwtTokenService.extractTokenId(active))).thenReturn(false);

        assertThat(service.isRevoked(revoked)).isTrue();
        assertThat(service.isRevoked(active)).isFalse();
    }

    @Test
    @DisplayName("a token issued before #676 (no jti) cannot be revoked and is not reported revoked")
    void tokenWithoutIdIsNotRevocable() {
        String legacy = Jwts.builder()
                .subject("admin")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        service.revoke(legacy);

        assertThat(service.isRevoked(legacy)).isFalse();
        verify(revokedTokenRepository, never()).save(any());
        verify(revokedTokenRepository, never()).existsById(anyString());
    }
}
