package com.licensis.notaire.domain.mfa;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.OptionalLong;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** TOTP per RFC 6238 (HMAC-SHA1, 6 digits, 30 s step). Plain JDK, no framework. */
public final class TotpAlgorithm {

    public static final int DIGITS = 6;
    public static final int PERIOD_SECONDS = 30;
    private static final int SKEW_STEPS = 1;
    private static final int MODULUS = 1_000_000;

    private TotpAlgorithm() {
    }

    public static long stepAt(Instant instant) {
        return instant.getEpochSecond() / PERIOD_SECONDS;
    }

    public static String code(byte[] secret, long step) {
        byte[] hash = hmacSha1(secret, ByteBuffer.allocate(Long.BYTES).putLong(step).array());
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);
        return String.format("%0" + DIGITS + "d", binary % MODULUS);
    }

    /** The step whose code equals {@code submitted}, within one step of {@code currentStep}. */
    public static OptionalLong matchingStep(byte[] secret, String submitted, long currentStep) {
        if (submitted == null || !submitted.matches("\\d{" + DIGITS + "}")) {
            return OptionalLong.empty();
        }
        byte[] expected = submitted.getBytes(StandardCharsets.US_ASCII);
        OptionalLong match = OptionalLong.empty();
        for (long step = currentStep - SKEW_STEPS; step <= currentStep + SKEW_STEPS; step++) {
            byte[] candidate = code(secret, step).getBytes(StandardCharsets.US_ASCII);
            if (MessageDigest.isEqual(candidate, expected)) {
                match = OptionalLong.of(step);
            }
        }
        return match;
    }

    private static byte[] hmacSha1(byte[] key, byte[] message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            return mac.doFinal(message);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA1 unavailable", e);
        }
    }
}
