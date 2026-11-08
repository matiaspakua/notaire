package com.licensis.notaire.domain.mfa;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.OptionalLong;

/** TOTP per RFC 6238 (HMAC-SHA1 built on MessageDigest because the domain may use java.* only, ADR-030 R3). */
public final class TotpAlgorithm {

    public static final int DIGITS = 6;
    public static final int PERIOD_SECONDS = 30;
    private static final int SKEW_STEPS = 1;
    private static final int MODULUS = 1_000_000;
    private static final int HMAC_BLOCK = 64;

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
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] block = key.length > HMAC_BLOCK ? sha1.digest(key) : key;
            byte[] inner = new byte[HMAC_BLOCK];
            byte[] outer = new byte[HMAC_BLOCK];
            for (int i = 0; i < HMAC_BLOCK; i++) {
                byte k = i < block.length ? block[i] : 0;
                inner[i] = (byte) (k ^ 0x36);
                outer[i] = (byte) (k ^ 0x5c);
            }
            sha1.update(inner);
            sha1.update(message);
            byte[] innerHash = sha1.digest();
            sha1.update(outer);
            return sha1.digest(innerHash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-1 unavailable", e);
        }
    }
}
