package com.licensis.notaire.config;

import com.licensis.notaire.business.RevokedToken;
import com.licensis.notaire.repository.RevokedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * Server-side revocation of individual JWTs on logout (issue #676). Stateless
 * tokens stay valid until they expire, so logout records the token id
 * ({@code jti}) in a denylist that {@link JwtAuthenticationFilter} checks. Only
 * the presented token is revoked: other sessions of the same user keep working.
 * Entries are kept until the token's own expiry and purged on the next logout.
 * Tokens issued before #676 carry no {@code jti} and simply expire.
 */
@Service
public class TokenRevocationService {

    private final JwtTokenService jwtTokenService;
    private final RevokedTokenRepository revokedTokenRepository;
    private final Supplier<Instant> clock;

    @Autowired
    public TokenRevocationService(JwtTokenService jwtTokenService,
                                  RevokedTokenRepository revokedTokenRepository) {
        this(jwtTokenService, revokedTokenRepository, Instant::now);
    }

    public TokenRevocationService(JwtTokenService jwtTokenService,
                                  RevokedTokenRepository revokedTokenRepository,
                                  Supplier<Instant> clock) {
        this.jwtTokenService = jwtTokenService;
        this.revokedTokenRepository = revokedTokenRepository;
        this.clock = clock;
    }

    /** Revokes a valid token; missing, invalid or id-less tokens are ignored. */
    @Transactional
    public void revoke(String token) {
        if (!jwtTokenService.isValid(token)) {
            return;
        }
        String jti = jwtTokenService.extractTokenId(token);
        if (jti == null || jti.isBlank()) {
            return;
        }
        Instant now = clock.get();
        revokedTokenRepository.deleteExpiredBefore(now);
        revokedTokenRepository.save(new RevokedToken(jti, jwtTokenService.extractExpiration(token), now));
    }

    /** True when the (already validated) token was revoked by a logout. */
    @Transactional(readOnly = true)
    public boolean isRevoked(String token) {
        String jti = jwtTokenService.extractTokenId(token);
        return jti != null && !jti.isBlank() && revokedTokenRepository.existsById(jti);
    }
}
