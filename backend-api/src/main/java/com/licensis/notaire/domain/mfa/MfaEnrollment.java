package com.licensis.notaire.domain.mfa;

import java.util.ArrayList;
import java.util.List;

/**
 * A user's TOTP enrolment. {@code secretBase32} is plaintext inside the domain only; the persistence adapter
 * encrypts it. {@code lastUsedStep} is -1 until a code has been accepted (replay protection).
 */
public record MfaEnrollment(
        int userId,
        String secretBase32,
        boolean confirmed,
        long lastUsedStep,
        List<String> recoveryCodeHashes) {

    public MfaEnrollment {
        recoveryCodeHashes = List.copyOf(recoveryCodeHashes);
    }

    public static MfaEnrollment begin(int userId, String secretBase32) {
        return new MfaEnrollment(userId, secretBase32, false, -1, List.of());
    }

    public MfaEnrollment confirm(long step, List<String> hashes) {
        return new MfaEnrollment(userId, secretBase32, true, step, hashes);
    }

    public MfaEnrollment withLastUsedStep(long step) {
        return new MfaEnrollment(userId, secretBase32, confirmed, step, recoveryCodeHashes);
    }

    public MfaEnrollment withoutRecoveryCodeHash(String hash) {
        List<String> remaining = new ArrayList<>(recoveryCodeHashes);
        remaining.remove(hash);
        return new MfaEnrollment(userId, secretBase32, confirmed, lastUsedStep, remaining);
    }
}
