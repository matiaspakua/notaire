package com.licensis.notaire.adapter.in.web.user;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.in.mfa.AssessLoginMfaUseCase;
import com.licensis.notaire.application.port.in.mfa.VerifySecondFactorUseCase;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.config.MfaTokenScope;
import com.licensis.notaire.domain.mfa.LoginMfaDecision;
import com.licensis.notaire.domain.user.User;

@Component
public class TokenLoginMfaGate implements LoginMfaGate {

    private final AssessLoginMfaUseCase assess;
    private final VerifySecondFactorUseCase secondFactor;
    private final JwtTokenService tokens;
    private final long challengeTtlMs;

    public TokenLoginMfaGate(AssessLoginMfaUseCase assess, VerifySecondFactorUseCase secondFactor,
            JwtTokenService tokens, @Value("${notaire.mfa.challenge-ttl-ms:300000}") long challengeTtlMs) {
        this.assess = assess;
        this.secondFactor = secondFactor;
        this.tokens = tokens;
        this.challengeTtlMs = challengeTtlMs;
    }

    @Override
    public Optional<Challenge> challenge(User user) {
        LoginMfaDecision decision = assess.assess(user.id(), user.type());
        return switch (decision) {
            case SESSION -> Optional.empty();
            case CHALLENGE -> Optional.of(new Challenge(token(user, MfaTokenScope.CHALLENGE), false));
            case ENROLLMENT_REQUIRED -> Optional.of(new Challenge(token(user, MfaTokenScope.ENROLLMENT), true));
        };
    }

    @Override
    public Optional<String> challengeSubject(String mfaToken) {
        return tokens.scopedSubject(mfaToken, MfaTokenScope.CHALLENGE);
    }

    @Override
    public boolean verifySecondFactor(User user, String totpCode, String recoveryCode) {
        return secondFactor.verify(user.id(), totpCode, recoveryCode);
    }

    private String token(User user, String scope) {
        return tokens.generateScopedToken(user.name(), scope, challengeTtlMs);
    }
}
