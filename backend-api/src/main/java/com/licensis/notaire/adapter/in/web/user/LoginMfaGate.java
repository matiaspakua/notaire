package com.licensis.notaire.adapter.in.web.user;

import java.util.Optional;

import com.licensis.notaire.domain.user.User;

/** Web-side facade over the MFA ports: short-lived tokens and the second-factor check (ADR-032). */
public interface LoginMfaGate {

    /** Present when the password-verified user must not receive a session yet. */
    Optional<Challenge> challenge(User user);

    /** Username inside a valid, unexpired challenge token. */
    Optional<String> challengeSubject(String mfaToken);

    boolean verifySecondFactor(User user, String totpCode, String recoveryCode);

    record Challenge(String token, boolean enrollmentRequired) { }
}
